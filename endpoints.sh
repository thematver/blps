curl -X POST -u user:password "http://localhost:3333/api/users/create?username=govno&password=zhopa"
curl -X POST -u user:password "http://localhost:3333/api/users/create?username=govno1&password=zhopa"
curl -X POST -u user:password "http://localhost:3333/api/users/create?username=govno2&password=zhopa"
curl -X POST -u user:password "http://localhost:3333/api/users/create?username=govno3&password=zhopa"
curl -X POST -u user:password "http://localhost:3333/api/users/create?username=govno4&password=zhopa"

# Create
curl -u user:password -X POST "http://localhost:3333/api/reviews?username=govno" -H "Content-Type: application/json" --data '{"title": "Test Review", "content": "This is a test review.", "username": "govno"}'

# Get by id
curl -u user:password -X GET "http://localhost:3333/api/reviews/2" -H "Accept: application/json"

# Get all reviews
curl -u user:password -X GET "http://localhost:3333/api/reviews" -H "Accept: application/json"

curl -u user:password -X POST "http://localhost:3333/api/users/permissions/govno" -H "Accept: application/json"
curl -u user:password -X POST "http://localhost:3333/api/users/permissions/govno2" -H "Accept: application/json"

# Get reviews from the queue
curl  -u moderator:password -X GET "http://localhost:3333/api/moderators/reviews" -H "Accept: application/json"

# Vote for review
curl -u moderator:password -X POST "http://localhost:3333/api/moderators/reviews/1/vote?approved=true" -H "Content-Type: application/json"

curl -u moderator:password -X POST "http://localhost:3333/api/moderators/reviews/1/vote?moderator=govno&approved=true" -H "Content-Type: application/json"
