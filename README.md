# group-service Project

http://localhost:8088/q/graphql-ui/

```graphql

#Mutations

mutation{
  addGroup(
    name: "ABC Group", 
    createdBy: "Duminda", 
    location: {
      type:"Feature", 
      geometry: {
        type: "Point", coordinates: [80.31888, 8.1007]
      },
      properties:{
        name: "ABC Group Location"
      }
    }
  )
}

#Queries 

query{
  allGroups(isBlackListed: false){
    location{
      geometry{
        coordinates
      }
      properties{
        name
      }
    }
    id
    createdBy
  }
}

# Fragments 

fragment Location on Group {
    location {
        type
        geometry {
            coordinates
            type
        }    properties {
            name
        }
    }
}

query GetGroups($isBlackListed: Boolean) {
    allGroups(isBlackListed: $isBlackListed) {
        id
        name
        createdDate
        ...Location
        __typename  }
}

```
