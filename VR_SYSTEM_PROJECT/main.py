from flask import Flask, request, jsonify
import cv2
import os
import numpy as np
import uuid

app = Flask(__name__)

UPLOAD_DIR = "/Users/artempodrezov/Router/uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)


@app.route('/process', methods=['POST'])
def process():
    if 'file' not in request.files:
        return jsonify({'error': 'No file part'}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    ext = os.path.splitext(file.filename)[-1]
    base_name = os.path.splitext(file.filename)[0]
    unique_id = uuid.uuid4().hex
    input_filename = f"{base_name}_{unique_id}{ext}"
    input_path = os.path.join(UPLOAD_DIR, input_filename)
    file.save(input_path)

    output_filename = f"{base_name}_{unique_id}_vr{ext}"
    output_path = os.path.join(UPLOAD_DIR, output_filename)

    apply_vr_effect_on_video(input_path, output_path)

    os.remove(input_path)

    return jsonify({'path': os.path.abspath(output_path)})


def apply_vr_effect(frame):
    shift = 10
    left_frame = np.roll(frame.copy(), -shift, axis=1)
    right_frame = np.roll(frame.copy(), shift, axis=1)
    return left_frame, right_frame


def apply_vr_effect_on_video(input_path, output_path):
    cap = cv2.VideoCapture(input_path)

    if not cap.isOpened():
        raise Exception(f"Error opening video file: {input_path}")

    frame_rate = cap.get(cv2.CAP_PROP_FPS)
    left_frames, right_frames = [], []

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break
        left, right = apply_vr_effect(frame)
        left_frames.append(left)
        right_frames.append(right)

    cap.release()

    if left_frames:
        h, w, _ = left_frames[0].shape
        out = cv2.VideoWriter(output_path, cv2.VideoWriter_fourcc(*'mp4v'), frame_rate, (w * 2, h))
        for l, r in zip(left_frames, right_frames):
            combined = np.hstack((l, r))
            out.write(combined)
        out.release()
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001)

