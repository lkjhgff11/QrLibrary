from flask import Flask, render_template, redirect, request, url_for
import main

app = Flask(__name__)


@app.route('/<query>')
def execute(query):
    response = main.process_query(query,',')
    return response
#HOST = '192.168.1.219'
HOST = '127.0.0.1'
PORT = 9877
if __name__ == '__main__':
    app.run(host = HOST, port = PORT)

