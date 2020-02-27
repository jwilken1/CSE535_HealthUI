from flask import Flask
from flask import request
import json

app = Flask(__name__)

@app.route('/download', methods=['GET'])
def download():
    return "Download your file!"
	
@app.route('/upload', methods=['POST'])
def upload():
	return "Upload your file!"

if __name__ == "__main__":
	app.run(host='localhost', port=5000)