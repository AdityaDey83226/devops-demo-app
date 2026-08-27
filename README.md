# devops-demo-app

Spring Boot Hello World app for the SAP Scholar DevOps Training (Aug 28, 2026).

Demonstrates the full DevOps story end-to-end:

```
Code → Git → CI/CD (GitHub Actions) → Build (Maven) → Deploy (BTP Cloud Foundry) → Kubernetes
```

---

## Run locally

Requires Java 17 and Maven.

```bash
cd app
mvn spring-boot:run
curl http://localhost:8080/
# Hello from SAP DevOps Training!
```

---

## Run with Docker

```bash
docker build -t devops-demo-app ./app
docker run -p 8080:8080 devops-demo-app
curl http://localhost:8080/
# Hello from SAP DevOps Training!
```

---

## CI/CD Pipeline (GitHub Actions)

The pipeline runs automatically on every push and pull request.

| Job | Trigger | What it does |
|-----|---------|--------------|
| **Test** | Every push + PR | `mvn test` — integration tests hit `/` and `/health` |
| **Build** | After test passes | `mvn package` — produces JAR, uploads as artifact `app-jar` |
| **Deploy** | Push to `main` only | Downloads JAR, authenticates to BTP CF, runs `cf push` |

### GitHub Secrets required

Add these in your fork: **Settings → Secrets and variables → Actions → New repository secret**

| Secret | Description |
|--------|-------------|
| `CF_API` | CF API endpoint, e.g. `https://api.cf.us10-001.hana.ondemand.com` |
| `CF_ORG` | CF org name |
| `CF_SPACE` | CF space name (usually `dev`) |
| `CF_REFRESH_TOKEN` | From `~/.cf/config.json` after `cf login --sso` |
| `CF_UAA_URL` | UAA endpoint from `~/.cf/config.json` |
| `CF_AUTH_URL` | Authorization endpoint from `~/.cf/config.json` |
| `CF_DOPPLER_URL` | Doppler endpoint from `~/.cf/config.json` |

Extract all values after `cf login -a <CF_API> --sso`:

```bash
cat ~/.cf/config.json | python3 -c "
import json, sys
d = json.load(sys.stdin)
print('CF_API:          ', d['Target'])
print('CF_ORG:          ', d['OrganizationFields']['Name'])
print('CF_SPACE:        ', d['SpaceFields']['Name'])
print('CF_REFRESH_TOKEN:', d['RefreshToken'])
print('CF_UAA_URL:      ', d['UaaEndpoint'])
print('CF_AUTH_URL:     ', d['AuthorizationEndpoint'])
print('CF_DOPPLER_URL:  ', d['DopplerEndPoint'])
"
```

> **Note:** CF refresh tokens are valid ~12 hours. Refresh the `CF_REFRESH_TOKEN` secret on the morning of the training session.

---

## Kubernetes (local minikube)

```bash
# 1. Start cluster
minikube start --driver=docker

# 2. Build image and load into minikube
docker build -t devops-demo-app ./app
minikube image load devops-demo-app

# 3. Deploy
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl get pods -w
# Wait for 3/3 pods Running

# 4. Access the app
kubectl port-forward service/devops-demo-app 8081:80
# In another terminal:
curl http://localhost:8081/
# Hello from SAP DevOps Training!

# 5. Self-healing demo — delete a pod, watch K8s restart it
kubectl delete pod <pod-name>
kubectl get pods -w

# 6. Scaling demo
kubectl scale deployment devops-demo-app --replicas=5
kubectl get pods -w

# 7. Clean up
kubectl delete -f k8s/deployment.yaml
kubectl delete -f k8s/service.yaml
minikube stop
```

---

## Repo structure

```
devops-demo-app/
├── app/                              # Spring Boot app
│   ├── pom.xml                       # Maven build (Spring Boot 3.3.2, Java 17)
│   ├── Dockerfile                    # Multi-stage build (JRE runtime, arm64-safe)
│   └── src/
│       ├── main/java/com/sap/training/
│       │   ├── Application.java
│       │   └── HelloController.java  # GET / and GET /health
│       ├── main/resources/
│       │   └── application.properties
│       └── test/java/com/sap/training/
│           └── ApplicationTests.java
├── .github/workflows/
│   └── ci-cd.yml                     # Test → Build → Deploy pipeline
├── k8s/
│   ├── deployment.yaml               # 3-replica Deployment
│   └── service.yaml                  # NodePort Service
├── manifest.yml                      # CF push config
└── README.md
```
