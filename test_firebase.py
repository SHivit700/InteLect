import firebase_admin
from firebase_admin import credentials, firestore
import datetime

try:
    cred = credentials.Certificate("ic_hack.json")
    firebase_admin.initialize_app(cred)
    db = firestore.client()

    doc_ref = db.collection('test_collection').document('test_doc')
    doc_ref.set({
        'message': 'Hello from local test',
        'timestamp': datetime.datetime.now()
    })
    print("Successfully wrote to Firestore!")
except Exception as e:
    print(f"Failed to write to Firestore: {e}")
