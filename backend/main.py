from flask import Flask, request, send_from_directory
import os
import requests
import secrets


app = Flask(__name__, static_url_path='')

@app.route('/')
def root():
    return app.send_static_file('index.html')

@app.route('/hello')
def hello():
    return 'Hello World! (Skipr Test)'

port = os.getenv('VCAP_APP_PORT', '5000')
if __name__ == "__main__":
    app.run(host='0.0.0.0', port=int(port), debug=True)