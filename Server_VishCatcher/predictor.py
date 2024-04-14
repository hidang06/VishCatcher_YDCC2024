from transformers import AlbertTokenizer, AlbertModel
import pandas as pd
import torch
from pydub import AudioSegment
import numpy as np
import io

# Load pre-trained ALBERT model and tokenizer
tokenizer = AlbertTokenizer.from_pretrained('albert-base-v2')
model = AlbertModel.from_pretrained('albert-base-v2')

from transformers import AutoTokenizer, AutoModelForSeq2SeqLM

model_name = "VietAI/envit5-translation"
tokenizer_vie = AutoTokenizer.from_pretrained(model_name)
model_vie = AutoModelForSeq2SeqLM.from_pretrained(model_name)

from keras.models import load_model
model_loaded = load_model('D:/Downloads/Code/deep_learning.h5')

import speech_recognition as sr
recognizer = sr.Recognizer()

def extract_features(text):
    inputs = tokenizer(text, return_tensors="pt", padding=True, truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**inputs)
    features = outputs.last_hidden_state.mean(dim=1).squeeze().numpy()  # Extract features from the last layer
    return features

def predict_text_recording(inputs):
  outputs = model_vie.generate(tokenizer_vie(inputs, return_tensors="pt", padding=True).input_ids.to('cpu'), max_length=512)
  outputs = (tokenizer_vie.batch_decode(outputs, skip_special_tokens=True))
  outputs = outputs[0][4:]
  print(outputs)

  text = outputs
  feature = extract_features(text)
  input_data = [feature]
  input_data = np.array(input_data)

  res = model_loaded.predict(input_data)
  return res[0]

def decode_audio(audio_bytes):
  try:
        print("decode_audio: ",audio_bytes[:20])
        # Đọc dữ liệu âm thanh từ bytes
        # audio_segment = AudioSegment.from_mp3(io.BytesIO(audio_bytes))
        audio_segment = AudioSegment.from_file(io.BytesIO(audio_bytes), format="mp4")
        
        # Chuyển đổi âm thanh thành định dạng WAV (vì speech_recognition không hỗ trợ MP3)
        wav_audio_bytes = audio_segment.export(format="wav").read()
        
        # Sử dụng speech_recognition để nhận dạng văn bản từ dữ liệu âm thanh WAV
        recognizer = sr.Recognizer()
        with sr.AudioFile(io.BytesIO(wav_audio_bytes)) as source:
            audio_data = recognizer.record(source)
        
        return audio_data
  except Exception as e:
        print("Error decoding audio:", e)
        return None
    
  return audio_data

def predict_recording(input):
  try:
    audio_data = decode_audio(input)
    text = recognizer.recognize_google(audio_data, language='vi-VN')
    res = predict_text_recording(text)
    return res
  except sr.UnknownValueError:
    print("Could not understand audio")
    return "error"
  except sr.RequestError as e:
    print("Could not request results from Google Speech Recognition service; {0}".format(e))
    return "gg-error"
  