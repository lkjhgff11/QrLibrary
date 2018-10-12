import socket
from pprint import pprint
s = socket.socket()
host = '127.0.0.1'
port = 9876

s.connect((host, port))
print( 'Connected to', host)

while True:
    z = input("Enter something for the server: ")
    s.send(z.encode('utf-8'))
    # Halts
    print ('[Waiting for response...]')
    response = (s.recv(1024)).decode('utf-8')
    #pprint (response)
    print(response)
