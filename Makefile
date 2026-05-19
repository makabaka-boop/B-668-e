.PHONY: test test-backend test-frontend test-docker test-docker-backend test-docker-frontend clean test-report

test: test-backend test-frontend

test-backend:
	@echo "Running backend tests..."
	cd backend && mvn test -Dspring.profiles.active=test

test-frontend:
	@echo "Running frontend tests..."
	cd frontend && npm run test

test-frontend-watch:
	@echo "Running frontend tests in watch mode..."
	cd frontend && npm run test:watch

test-frontend-coverage:
	@echo "Running frontend tests with coverage..."
	cd frontend && npm run test:coverage

test-docker:
	@echo "Running all tests in Docker..."
	docker compose -f docker-compose.test.yml up --build --abort-on-container-exit
	docker compose -f docker-compose.test.yml down

test-docker-backend:
	@echo "Running backend tests in Docker..."
	docker compose -f docker-compose.test.yml up --build backend-test --abort-on-container-exit
	docker compose -f docker-compose.test.yml down

test-docker-frontend:
	@echo "Running frontend tests in Docker..."
	docker compose -f docker-compose.test.yml up --build frontend-test --abort-on-container-exit
	docker compose -f docker-compose.test.yml down

test-report:
	@echo "Generating test reports..."
	cd backend && mvn surefire-report:report
	cd frontend && npm run test:coverage

clean:
	@echo "Cleaning test artifacts..."
	cd backend && mvn clean
	cd frontend && rm -rf test-results coverage
	docker compose -f docker-compose.test.yml down -v
