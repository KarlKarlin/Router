from fastapi import FastAPI
from transformers import pipeline

app = FastAPI()

model = pipeline("text-classification", model="distilbert-base-uncased-finetuned-sst-2-english")

@app.get("/predict_genre/")
def predict_genre(text: str):
    emotion_result = model(text)
    emotion = emotion_result[0]['label'].lower()

    emotion_to_genre = {

        # Positive emotions
        'joy': 'Comedy',
        'happy': 'Comedy',
        'happiness': 'Comedy',
        'elated': 'Comedy',
        'excited': 'Adventure',
        'amused': 'Comedy',
        'cheerful': 'Comedy',
        'positive': 'Comedy',
        'contentment': 'Romance',
        'funny': 'Comedy',
        'grateful': 'Family',
        'optimistic': 'Fantasy',
        'hopeful': 'Fantasy',
        'enthusiastic': 'Adventure',
        'proud': 'Biography',
        'love': 'Romance',
        'affection': 'Romance',
        'caring': 'Family',
        'confident': 'Biography',
        'calm': 'Documentary',
        'trust': 'Biography',
        'relaxed': 'Slice of Life',

        # Neutral / Mixed
        'neutral': 'Documentary',
        'unsure': 'Mystery',
        'confused': 'Mystery',
        'curious': 'Sci-Fi',
        'nostalgia': 'Historical',
        'reflective': 'Drama',
        'tired': 'Slice of Life',
        'bored': 'Action',
        'peaceful': 'Documentary',

        # Negative emotions
        'anger': 'Crime',
        'angry': 'Crime',
        'annoyed': 'Crime',
        'frustrated': 'Thriller',
        'fear': 'Horror',
        'afraid': 'Horror',
        'scared': 'Horror',
        'nervous': 'Thriller',
        'worried': 'Thriller',
        'anxious': 'Thriller',
        'tense': 'Thriller',
        'panic': 'Thriller',
        'surprise': 'Thriller',
        'shocked': 'Thriller',
        'sad': 'Drama',
        'sadness': 'Drama',
        'lonely': 'Drama',
        'depressed': 'Drama',
        'disappointed': 'Drama',
        'hurt': 'Drama',
        'grief': 'Drama',
        'melancholy': 'Drama',
        'guilt': 'Drama',
        'disgust': 'Horror',
        'resentful': 'Crime',
        'jealous': 'Drama',
        'negative': 'Drama',

        # Rare / Specific emotions
        'embarrassed': 'Comedy',
        'awkward': 'Comedy',
        'shy': 'Romance',
        'surprised': 'Thriller',
        'awe': 'Fantasy',
        'envy': 'Drama',
        'inspired': 'Biography',
        'determined': 'Action',
        'motivated': 'Action',
        'triumphant': 'Action',
        'vindicated': 'Crime',
        'revengeful': 'Crime',
        'homesick': 'Drama',
        'overwhelmed': 'Drama',

    }

    genre = emotion_to_genre.get(emotion, "Unknown")
    return genre

if __name__ == '__main__':
    import uvicorn
    uvicorn.run(app, host='0.0.0.0', port=5002)