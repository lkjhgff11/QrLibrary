#! /usr/bin/python
# -*- coding: utf-8 -*-

import main
from socket import *
from select import *
import sys

HOST = '127.0.0.1'
#HOST = '192.168.0.3'
PORT = 9876
BUFSIZE = 1024
ADDR = (HOST,PORT)


print('#### Library Server ####')
serverSocket = socket(AF_INET, SOCK_STREAM)
serverSocket.bind(ADDR)

serverSocket.listen(100)

while 1:
    print('#####')
    
    clientSocket, addr_info = serverSocket.accept()
    query = clientSocket.recv(1024)
    print('@@@@')
    print(query)
    print('Formatting : %s' % 'query')
    try:
        query = query.decode('utf-8')#[2:]
    except:
        continue
    if not query:
        continue

    print('Request]',query)
    print('Formatting : Request] %s' % 'query')

    response = str(main.process_query(query))
    
    print('Response]',response)
    print('Formatting : Response] %s' % 'response')
    clientSocket.send(response.encode('utf-8'))
        


#clientSocket.close()

#serverSocket.close()

print('Close')
