from flask import Flask
import requests
import secrets


app = Flask(__name__)

@app.route('/')
def hello():
    return 'Hello World! Skipr Test :D'

if __name__ == '__main__':
    app.run(debug=True)