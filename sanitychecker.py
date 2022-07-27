import requests


#Modified version of setupData to test dijsktra algorithm
#

userUrl = 'http://localhost:8001'
locationUrl = 'http://localhost:8000'
tripUrl = 'http://localhost:8002'

#Configurations
userObj = {
  "name": "TestName354",
  "email": "test@gmail.com",
  "password": "ILoveCS"
}

#Drivers
Drivers = [
  {
  "name": "Ritvik",
  "email": "ritvik@gmail.com",
  "password": "Ritvik"
},
{
  "name": "Kyrel",
  "email": "Kyrel@gmail.com",
  "password": "Kyrel"
},
]


Roads = [
  {
    "roadName": "a",
    "hasTraffic": True
  },
  {
    "roadName": "b",
    "hasTraffic": False
  },
  {
    "roadName": "c",
    "hasTraffic": True
  },
  {
    "roadName": "d",
    "hasTraffic": False
  },
  {
    "roadName": "e",
    "hasTraffic": True
  },
  { #Not connected to anything
    "roadName": "f",
    "hasTraffic": True
  }
]

RoadConnections = {
  "a": [("b", 1), ("c", 6)],
  "b": [("c", 2),("d", 1)],
  "c": [("d",2),("e",5)],
  "d": [("e",5)],
}

def createUser(userObj, isDriver):
  req = requests.post(userUrl + '/user/register', json=userObj)
  uid = None
  if (req.status_code == 200):
    uid = req.json()['uid']
    req = requests.put(locationUrl + '/location/user', json={"uid": uid, "is_driver": isDriver})
    print(req)
    if (req.status_code == 200):
      req = requests.patch(locationUrl + f'/location/{uid}',json={"latitude": 0.0, "longitude": 0.0, "street": "Liut Lights"})
      print(req)
  return uid

print("Creating User...")
# Create user
createUser(userObj, False)

print("Creating Drivers...")
for driver in Drivers:
  uid = createUser(driver, True)
  if uid != None:
    req = requests.patch(userUrl + f'/user/{uid}', json={"isDriver": True})
    print(req)

print("Creating Roads...")
# Create Roads
for road in Roads:
  req = requests.put(locationUrl + '/location/road', json=road)
  print(req)

print("Creating Road Connections...")
# Create Road Conncetions
for roadOne, roadTups in RoadConnections.items():
  for roadTwo, time in roadTups:
    req = requests.post(locationUrl + '/location/hasRoute',json = {
      "roadName1": roadOne,
      "roadName2": roadTwo,
      "time": time,
      "hasTraffic": False
    })
    print(req)

    # These are all two way roads
    req = requests.post(locationUrl + '/location/hasRoute',json = {
      "roadName1": roadTwo,
      "roadName2": roadOne,
      "time": time,
      "hasTraffic": False
    })
    print(req)

print("Done creating data.")

