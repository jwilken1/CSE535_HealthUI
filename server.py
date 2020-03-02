import flask
import werkzeug

app = flask.Flask(__name__)

@app.route('/', methods=['GET'])
def download():
    try:
        return flask.send_file('./databases/jwilkens.db', as_attachment=True)
    except Exception as e:
        return str(e)
	
@app.route('/', methods=['POST'])
def upload():
    print("Post Recieved")
    database_file = flask.request.files['uploaded_file']
    filename = werkzeug.utils.secure_filename(database_file.filename)
    print("\nReceived Database File : " + database_file.filename)
    database_file.save('./databases/' + filename)
    return "Database Uploaded Successfully"

if __name__ == "__main__":
	app.run(host='localserver', port=5000)