import sqlite3

# 유저들의 정보가 담겨있다.
# 1개의 유저그룹에 다수의 도서관이 매핑될 수도 있다.
# ex) 부경대 => 부경대 중앙도서관, 부경대 구학도서관
class UserGroup():
    def __init__(self, name, init = False):
        self.db_name = name + '.ug'
        conn, cursor = self.get_cc()
        if init:
            cursor.execute("select 'drop table ' || name || ';' from sqlite_master where type = 'table';")
            
        cursor.execute("""
            CREATE TABLE if not exists user
            (
                id char(10) PRIMARY KEY,
                pw char(10),
                name char(10)
            );
            """
        )
        conn.close()

    def get_cc(self):
        conn = sqlite3.connect(self.db_name, isolation_level=None)
        cursor = conn.cursor()
        cursor.execute('pragma foreign_keys = 1')
        return conn, cursor

    # 로그인
    # 성공이라면 100
    # 아이디가 존재하지 않으면 201, 비밀번호가 틀렸으면 202를 반환한다.
    def sign_in(self, user_id, pw):
        conn, cursor = self.get_cc()
        cursor.execute("select pw from user where id = ?", (user_id,))
        matched_pw = cursor.fetchone()
        conn.close()
        if not matched_pw:
            return 201
        if matched_pw[0] != pw:
            return 202
        return 100
        
    def show_all_user(self):
        conn, cursor = self.get_cc()
        with conn:
            cursor.execute('select * from user')
            users = cursor.fetchall()
        return users

    def add_user(self, sid, pw, name):
        conn, cursor = self.get_cc()
        with conn:
            print(name)
            cursor.execute('insert into user values (?, ?, ?)',(sid, pw, name))


if __name__ == "__main__":
    ug = UserGroup('pknu')
    
    
