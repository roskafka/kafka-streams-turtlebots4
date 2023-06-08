from pydantic import BaseModel


class PayloadPosition(BaseModel):
    x: float
    y: float
    theta: float
    linear_velocity: float
    angular_velocity: float


class Vector3(BaseModel):
    x: float
    y: float
    z: float


class PayloadVelocity(BaseModel):
    linear: Vector3
    angular: Vector3


class PayloadBackgroundColor(BaseModel):
    r: int
    g: int
    b: int


class MetaData(BaseModel):
    mapping: str
    source: str
    type: str


class Message(BaseModel):
    payload: PayloadPosition | PayloadVelocity
    metadata: MetaData
