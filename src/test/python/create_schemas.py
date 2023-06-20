# TODO: move to src/main/python
import json
from pathlib import Path
import requests

# current directory: src/test/python
# avro path: src/main/avro
avro_schemas_path = Path(__file__).parent.parent.parent / "main" / "avro"
schema_registry_url = "http://localhost:8081"

schemas = [
    {
        "file": "input/diagnostics.avsc",
        "topic": "in_1",
    },
    {
        "file": "input/diagnostics.avsc",
        "topic": "out_1",
    }
]

# publish schemas to schema registry
for schema in schemas:
    print(f"Publishing {schema['file']} to {schema['topic']}")
    with open(avro_schemas_path / schema["file"], "r") as f:
        schema_content = {
            "schema": f.read()
        }
        res = requests.post(f"{schema_registry_url}/subjects/{schema['topic']}-value/versions",
                      data=json.dumps(schema_content),
                      headers={"Content-Type": "application/vnd.schemaregistry.v1+json"})
        if not res.ok:
            print(f"Error: {res.text} {res.status_code}")
            exit(1)
