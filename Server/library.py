from time import time

import json
import sqlite3
import string
import random


class Library():

    def get_cc(self):
        
        conn = sqlite3.connect(self.db_name, isolation_level=None)
        cursor = conn.cursor()
        cursor.execute('pragma foreign_keys = 1')
        return conn, cursor

    def __init__(self, name, init = False):
        
        name = name.replace('/','_')
        self.db_name = name + '.lb'
        self.default_life       = 120 # 처음 앉으면 몇분 이후에 마감되는지
        self.extending_time     = 120 # 한번 연장에 몇분씩 늘어나는
        self.extending_min_time =  30 # 최소 몇분 이하부터 연장 가능한지
        
        conn, cursor = self.get_cc()

        if init:
            cursor.execute("select 'drop table ' || name || ';' from sqlite_master where type = 'table';")
        
        # 구역
        cursor.execute('create table if not exists section (id char(10) PRIMARY KEY)')

        # 좌석
        cursor.execute("""
            create table if not exists seat
            (
                section_id char(10) REFERENCES section(id) ON DELETE CASCADE,
                id int(10),
                qr char(10),
                PRIMARY KEY(id, section_id)
            )
            """
        )

        # 착석
        cursor.execute("""
            create table if not exists seating
            (
                section_id char(10) REFERENCES section(id) ON DELETE CASCADE, 
                seat_id int(10),
                user_id char(10),
                seated_time datetime,
                expired_time datatime,
                PRIMARY KEY(section_id, seat_id),
                FOREIGN KEY(seat_id, section_id) REFERENCES seat(id, section_id) ON DELETE CASCADE
                UNIQUE(user_id)
            )
            """
        )
        
        conn.close()

    def update(self):
        conn, cursor = self.get_cc()
        user_ids = cursor.execute('select user_id from seating where expired_time < ?', (time(),)).fetchall()
        
        for user_id in user_ids:
            print("TIME OVER === ",user_id[0])
            self.leave(user_id[0])
        conn.close()
        
    # 모든 층들의 정보를 얻는다.
    def get_all_seats_data(self):
        dic = {}
        conn, cursor = self.get_cc()
        sections = cursor.execute('select * from section').fetchall()        
        for section in sections:
            section = section[0]
            dic[section] = []
            seats = cursor.execute('select * from seat where section_id = ?', (section,)).fetchall()
            for seat in seats:
                section_id, seat_id, qr = seat
                dic[section].append(self.seat_to_dic(section_id, seat_id))
        conn.close()
        j = json.dumps(dic, ensure_ascii=False)
        return j

    # 좌석에 대한 정보를 json으로 반환
    def seat_to_json(self, section, index):
        dic = self.seat_to_dic(section, index)
        return json.dumps(dic)
    
    # 좌석에 대한 정보를 dictionary로 반환
    def seat_to_dic(self, section, index):
        dic = {}
        seating = self.get_seating_as_index(section, index)
        if seating:
            user_id, seated_time, user_id, seated_time, expired_time = seating
            dic['state'] = 'taken'
            dic['seating_time'] = seated_time
            dic['exp_time'] = expired_time
        else:
            dic['state'] = 'empty'
            dic['seating_time'] = dic['exp_time'] = 0
        return dic

    # 특정 좌석의 착석에 대한 정보를 가져온다.
    # 없으면 None을 반환
    def get_seating_as_index(self, section, index):
        conn, cursor = self.get_cc()
        cursor.execute("""
            select *
            from seating where section_id = ? and seat_id = ?
        """, (section, index))
        seating = cursor.fetchone()
        conn.close()
        if not seating:
            return None
        return seating
    
    # 유저아이디로 착석정보를 받아온다.
    # 없으면 None을 반환
    def get_seating_as_user_id(self, user_id):
        conn, cursor = self.get_cc()
        cursor.execute('select * from seating where user_id = ?',(user_id,))
        seating = cursor.fetchone()
        conn.close()
        if not seating:
            return None
        return seating

    def get_user_seat(self, user_id):
        seating = self.get_seating_as_user_id(user_id)
        if not seating:
            return 203
        section, seat, user_id, seated_time, expired_time = seating
        return 100, section, seat, self.seat_to_dic(section, seat)
    
    # 앉는다.
    #성공 3-2-100		
    #실패 3-2-201 QR 틀림	
    #실패 3-2-202 이미 다른 유저가 있음
    #실패 3-2-206 이미 자신이 다른 자리에 있음
    def seat(self, section, seat, user_id, qr):
        conn, cursor = self.get_cc()
        cursor.execute('select qr from seat where section_id = ? and id = ?',(section, seat))
        target_qr = self.get_qr(section, seat)

        # qr이 틀렸다면 201을 반환한다.
        if qr != target_qr:
            conn.close()
            return 201
        
        # 이미 누군가 앉아 있다면 202를 반환한다.
        if self.get_seating_as_index(section, seat):
            return 202

        if self.get_seating_as_user_id(user_id):
            return 206
        
        seating_time = time()
        expired_time = seating_time + 60 * self.default_life
        query = 'insert into seating values(?,?,?,?,?)'
        cursor.execute(query,(section, seat, user_id, seating_time, expired_time))
        conn.close()
        return 100

    # 연장한다.
    # qr이 틀렸다면 201을 반환한다.
    # 본인이 앉은 좌석이 없다면 203을 반환한다.
    # 연장 가능한 상태가 아니면 206을 반횐
    def extend(self, user_id, qr):
        conn, cursor = self.get_cc()
        seating = cursor.execute('select section_id, seat_id, expired_time from seating where user_id = ?',(user_id,)).fetchone()
        conn.close()
        
        # 본인이 앉은 좌석이 없다면 203을 반환한다.
        if not seating:
            return 203
                
        section, seat, expired_time = seating
        # qr이 틀렸다면 201을 반환한다.
        if self.get_qr(section, seat) != qr:
            return 201

        print("can extend ",self.can_extend(user_id))
        # 연장 가능한 상태가 아니면 206을 반횐
        if self.can_extend(user_id) != 100:
            return 206

        expired_time += self.extending_time * 60
        conn, cursor = self.get_cc()
        seating = cursor.execute("""
            update seating
            set expired_time = ?
            where user_id = ?
        """, (expired_time, user_id))
        conn.close()
        return 100, expired_time

    # 퇴실
    # 성공시 100, 자신의 자리가 없을 시 203
    def leave(self, user_id):
        conn, cursor = self.get_cc()
        seating = cursor.execute('select section_id, seat_id from seating where user_id = ?',(user_id,)).fetchone()
        
        if not seating:
            conn.close()
            return 203
        cursor.execute('delete from seating where user_id = ?',(user_id,))
        return 100
        conn.close()

    # 유저가 자신의 좌석을 연장 가능한가
    def can_extend(self, user_id):
        conn, cursor = self.get_cc()
        seating = cursor.execute('select expired_time from seating where user_id = ?',(user_id,)).fetchone()
        conn.close()

        # 자신의 좌석이 없다면 203을 반환한다.
        if not seating:
            return 203
        expired_time = seating[0]
        remaining_time = (expired_time - time()) / 60

        # 최소시간을 만족하지 못한다면 201을 반환한다.
        if self.extending_min_time < remaining_time:
            return 201
        return 100
        
        
    # 특정 좌석의 qr코드 값을 반환한다.
    def get_qr(self, section, seat):
        conn, cursor = self.get_cc()
        qr = cursor.execute('select qr from seat where section_id = ? and id = ?',(section, seat)).fetchone()
        if qr == None:
            return None
        conn.close()
        return qr[0]
       
    # 새로운 층을 추가한다. 
    def add_section(self, section_id, qr_codes):
        conn, cursor = self.get_cc()
        cursor.execute("insert into section values(?)", (section_id, ))
        for i in range(len(qr_codes)):
            qr_code = qr_codes[i]
            conn.execute("insert into seat values(?, ?, ?)",(section_id, i+1, qr_code))
        conn.close()



