from user_group import UserGroup
from library import Library
from library_manager import LibraryManager
from pprint import pprint
from inspect import signature
from collections import Iterable

import sqlite3

lib_to_user = {
    '대연/중앙도서관' : 'pknu'
}

user_pknu = [
    ['201412839','1234','choe'],
    ['201412840','1235','choe2'],
]

data_pklib = {
    '제1열람실' : ['aaa','bbb','ccc', 'ddd', 'eee', 'fff'],
    '제2열람실' : ['aaa','bbb','ccc', 'ddd', 'eee', 'fff'],
    '노트북실' : ['aaa','bbb','ccc', 'ddd', 'eee', 'fff']
}  
        
def init():
    manager = LibraryManager(True)
    for key in lib_to_user:
        manager.add_link(key, lib_to_user[key])
    
    users = UserGroup('pknu', True)
    for user_data in user_pknu:
        sid, pw, name = user_data
        users.add_user(sid, pw, name)
        
    pklib = Library('대연/중앙도서관', True)
    for section_id in data_pklib:
        qr_codes = data_pklib[section_id]
        pklib.add_section(section_id, qr_codes)

def sign_in(campus, uid, pw):
    ug = UserGroup(campus)

def show_table(db_name, table_name):
    conn = sqlite3.connect(db_name)
    cursor= conn.cursor()
    for d in cursor.execute('select * from ' + table_name).fetchall():
        print(d)
    conn.close()

manager = LibraryManager()

    
actions = {
    (1,1) : manager.sign_in,
    (2,1) : manager.get_all_seats_data,
    (3,1) : manager.get_seat_data,
    (3,2) : manager.seat,
    (3,3) : manager.extend,
    (3,4) : manager.leave,
    (3,5) : manager.get_user_seat,
    (4,1) : manager.get_extending_time,
    (4,2) : manager.get_extending_min_time,
    (4,3) : manager.can_extend 
}

def process_query(query, sep = '/'):
    spliteds = query.split(sep)
    # 인자의 갯수, 형식이 문제일 때1
    if len(spliteds) < 2:
        return
    try: # 인자의 갯수, 형식이 문제일 때2
        header = (int(spliteds[0]), int(spliteds[1]))
        str_header = sep.join(map(str,header))
    except IndexError as e:
        print(e)
        return str_header+sep+'400'
    parms = spliteds[2:]
    if not(header in actions.keys()): # 헤더가 존재하지 않으면 600을 반환
        return str_header+sep+'600'
    action = actions[header]
    sig = signature(action)
    params = sig.parameters
    if len(params) != len(parms):
        return str_header+sep+'400'
    try:
        response = action(*parms)
        if type(response) == tuple:
            response = sep.join(map(str,response))
        return str_header+sep+str(response)
        
    except Exception as e:
        raise e
        return str_header+sep+'300'
    
