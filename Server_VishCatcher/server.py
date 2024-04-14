from flask import Flask, request, jsonify
import predictor

app = Flask(__name__)

@app.route('/upload', methods=['POST'])
def upload_file():
    if 'audio' not in request.files:
        return jsonify({'message': 'No file part'}), 400

    file = request.files['audio']
    
    file_byte = file.read()
    print(file_byte[:20])
    res = predictor.predict_recording(file_byte)
    print(res)

    if file.filename == '':
        return jsonify({'message': 'No selected file'}), 400

    # Lưu file và xử lý dữ liệu tại đây nếu cần
    if (res <= 0.5):
        return '0', 200
    return '1', 200

if __name__ == '__main__':
    app.run(host='172.28.240.209', port=5000, debug=True)
