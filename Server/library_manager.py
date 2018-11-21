from user_group import UserGroup
from library import Library
from time import time, sleep

import threading
import random
import string
import sqlite3

def random_string(size=10, chars=string.ascii_uppercase + string.digits):
    return ''.join(random.choice(chars) for _ in range(size))

# 도서관 관리 프로그램
class LibraryManager(threading.Thread):
    
    def get_cc(self):
        conn = sqlite3.connect('manager.db', isolation_level=None)
        cursor = conn.cursor()
        cursor.execute('pragma foreign_keys = 1')
        return conn, cursor

    def __init__(self, init = False):
        threading.Thread.__init__(self)
        self.user_groups = {}
        self.librarys = {}
        conn, cursor = self.get_cc()
        
        if init:
            cursor.execute("select 'drop table ' || name || ';' from sqlite_master where type = 'table';")
            
        # 도서관과 유저그룹을 연결
        cursor.execute("""
            create table if not exists lib_to_ug
                (lib_id char(10), ug_id char(10))
        """)
        
        # 로그인 토큰에 관한 테이블
        cursor.execute("""
            create table if not exists sign_in
            (
                token char(10),
                lib_id char(10),
                user_id char(10),
                signed_time datetime,
                PRIMARY KEY(lib_id, user_id)
            )
        """)
        conn.close()
        self.start()

    def add_link(self, lib_id, ug_id):
        conn, cursor = self.get_cc()
        cursor.execute('insert into lib_to_ug values(?, ?)', (lib_id, ug_id))
        conn.close()

    def run(self):
        while True:
            sleep(1)
            for lib_id in self.librarys:
                library = self.librarys[lib_id]
                library.update()

    # 라이브러리의 이름과 매핑되는 유저그룹
    def get_user_group_as_library(self, lib_id):
        conn, cursor = self.get_cc()
        ug = cursor.execute('select ug_id from lib_to_ug where lib_id = ?',(lib_id,)).fetchone()
        conn.close()
        # 유저그룹이 없다면
        if not ug:
            return None
        return self.get_user_group(ug[0])
    
    def get_user_group(self, ug_id):
        if not (ug_id in self.user_groups.keys()):
            self.user_groups[ug_id] = UserGroup(ug_id)
        return self.user_groups[ug_id]

    def get_library(self, lib_id):
        if not (lib_id in self.librarys.keys()):
            self.librarys[lib_id] = Library(lib_id)
        return self.librarys[lib_id]

    # 로그인 정보를 반환한다.
    def get_sign_data(self, token):
        conn, cursor = self.get_cc()
        data = cursor.execute('select * from sign_in where token = ?', (token, )).fetchone()
        conn.close()
        return data
    
    # 로그인 토큰으로 도서관을 반환한다.
    def get_library_as_token(self, token):
        sign_data = self.get_sign_data(token)
        if not sign_data:
            print('Null sign data',token)
            return None
        token, lib_id, user_id, signed_time = sign_data
        return self.get_library(lib_id)

    # 로그인 토큰으로 유저의 id를 반환한다.
    # 잘못된 토큰이라면 None을 반환한다.
    def get_user_id_as_token(self, token):
        sign_data = self.get_sign_data(token)
        if not sign_data:
            return None
        token, lib_id, user_id, signed_time = sign_data
        return user_id

        
    # 1-1 로그인
    #성공 1-1-100 token
    #실패 1-1-201 없는아이디 or 비번틀림
    #실패 1-1-202 비번틀림
    #실패 1-1-205 없는 도서관
    def sign_in(self, lib_id, user_id, pw):
        
        # 도서관 정보와 해당 도서관의 유저데이터를 가져온다.
        lib = self.get_library(lib_id)
        ug = self.get_user_group_as_library(lib_id)
        if not ug:
            return 205
        
        # id와 pw로 로그인이 가능한지 확인한다.
        sign_res = ug.sign_in(user_id, pw)
        if sign_res != 100:
            return sign_res

        # 로그인이 성공했다면 토큰을 기록한다.
        token = random_string()
        conn, cursor = self.get_cc()

        # 이전에 로그인 한 기록을 확인해서
        query = 'select * from sign_in where lib_id = ? and user_id = ?'
        prev_token = cursor.execute(query, (lib_id, user_id)).fetchone()
        if prev_token: # 아직 이전의 토큰이 남아있다면 갱신하고
            cursor.execute("""
                update sign_in
                set signed_time = ?, token = ?
                where user_id = ? and lib_id = ?
            """, (time(), token, user_id, lib_id)
            )
            
        else: # 첫 로그인이라면 새로운 토큰을 추가한다.
            cursor.execute("insert into sign_in values(?, ?, ?, ?)", (token, lib_id, user_id, time()))
        conn.close()

        print(sign_res,token,'-------------')
        return sign_res, token

    # 모든좌석정보 요청 2-1	token
    # 도서관이 존재하지 않는다면 ..?
    def get_all_seats_data(self, token):
        library = self.get_library_as_token(token)
        if not library:
            print(token)
        return library.get_all_seats_data()
        
    # 좌석 세부정보 3-1	token, section, seat index
    def get_seat_data(self, token, section, seat):
        library = self.get_library_as_token(token)
        return library.seat_to_json(section, seat)

    # 자리 신청	3-2 token , section, seat index, qr
    # 토큰이 존재하지 않는다면 301 반환
    def seat(self, token, section, seat, qr):
        user_id = self.get_user_id_as_token(token)
        if not user_id:
            return 301
        library = self.get_library_as_token(token)
        res = library.seat(section, seat, user_id, qr)
        return res

    #연장 3-3     token, qr
    def extend(self, token, qr):
        user_id = self.get_user_id_as_token(token)
        library = self.get_library_as_token(token)
        return library.extend(user_id, qr)

    # 퇴실 3-4    token
    # 성공시 100, 자신의 자리가 없을 시 203
    def leave(self, token):
        user_id = self.get_user_id_as_token(token)
        if not user_id: # 자신의 자리가 없을 시 203
            return 203
        library = self.get_library_as_token(token)
        return library.leave(user_id)

    def get_user_seat(self, token):
        user_id = self.get_user_id_as_token(token)
        library = self.get_library_as_token(token)
        return library.get_user_seat(user_id)

    # 1회당 연장시간	4-1
    def get_extending_time(self):
        return self.get_library('template').extending_time

    # 연장 최소시간	4-2
    def get_extending_min_time(self):
        return self.get_library('template').extending_min_time

    # 연장 가능한가	4-3	token
    # 자신의 자리가 없을 시 203
    def can_extend(self, token):
        user_id = self.get_user_id_as_token(token)
        if not user_id: # 자신의 자리가 없을 시 203
            return 203
        library = self.get_library_as_token(token)
        return library.can_extend(user_id)
