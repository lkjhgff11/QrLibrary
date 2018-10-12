import socket
import main
from pprint import pprint

while True:
   try:
      s = socket.socket()
      host = '192.168.1.219'
      port = 9876
      s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
      s.bind((host, port))
   
      s.listen(5)
      c = None
   
      while True:
         if c is None:
             # Halts
             print( '[Waiting for connection...]')
             c, addr = s.accept() #  (socket object, address info) return
             print( 'Got connection from', addr)
         else:
             # Halts
             print( '[Waiting for response...]')
             request = (c.recv(1024)).decode('utf-8')[2:]
             #if not request:
              #  continue
             response = str(main.process_query(request,','))
             print('REQ]',request)
             print('RES]',response)
             c.send(response.encode('utf-8'))
   except ConnectionAbortedError as e:
      print('break------------')
      continue
