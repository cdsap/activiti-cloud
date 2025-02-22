RELEASE_VERSION := $(or $(shell cat VERSION), $(shell python -c "from xml.etree.ElementTree import parse; print(parse(open('pom.xml')).find('{http://maven.apache.org/POM/4.0.0}version').text)"))
ACTIVITI_CLOUD_FULL_CHART_CHECKOUT_DIR := .git/activiti-cloud-full-chart
ACTIVITI_CLOUD_FULL_EXAMPLE_DIR := $(ACTIVITI_CLOUD_FULL_CHART_CHECKOUT_DIR)/charts/activiti-cloud-full-example
ACTIVITI_CLOUD_FULL_CHART_BRANCH := dependency-activiti-cloud-application-$(RELEASE_VERSION)
ACTIVITI_CLOUD_FULL_CHART_RELEASE_BRANCH := $(or $(ACTIVITI_CLOUD_FULL_CHART_RELEASE_BRANCH),develop)

updatebot/push-version:
	$(eval ACTIVITI_CLOUD_VERSION=$(shell python -c "from xml.etree.ElementTree import parse; print(parse(open('activiti-cloud-dependencies/pom.xml')).find('.//{http://maven.apache.org/POM/4.0.0}activiti-cloud.version').text)"))
	updatebot push-version --kind maven \
		org.activiti.cloud:activiti-cloud-dependencies $(RELEASE_VERSION) \
		org.activiti.cloud:activiti-cloud-modeling-dependencies $(ACTIVITI_CLOUD_VERSION) \
		org.activiti.cloud:activiti-cloud-audit-dependencies $(ACTIVITI_CLOUD_VERSION) \
		org.activiti.cloud:activiti-cloud-api-dependencies $(ACTIVITI_CLOUD_VERSION) \
		org.activiti.cloud:activiti-cloud-parent $(ACTIVITI_CLOUD_VERSION) \
		org.activiti.cloud:activiti-cloud-connectors-dependencies $(ACTIVITI_CLOUD_VERSION) \
		org.activiti.cloud:activiti-cloud-messages-dependencies $(ACTIVITI_CLOUD_VERSION) \
		org.activiti.cloud:activiti-cloud-modeling-dependencies $(ACTIVITI_CLOUD_VERSION) \
		org.activiti.cloud:activiti-cloud-notifications-graphql-dependencies $(ACTIVITI_CLOUD_VERSION) \
		org.activiti.cloud:activiti-cloud-query-dependencies $(ACTIVITI_CLOUD_VERSION) \
		org.activiti.cloud:activiti-cloud-runtime-bundle-dependencies $(ACTIVITI_CLOUD_VERSION) \
		org.activiti.cloud:activiti-cloud-service-common-dependencies $(ACTIVITI_CLOUD_VERSION) \
		--merge false

install: release
	echo helm $(helm version --short)
	test $(MESSAGING_BROKER) ||  exit 1
	test $(MESSAGING_PARTITIONED) ||  exit 1
	test $(MESSAGING_DESTINATIONS) ||  exit 1

	yq e -i '(.* | select(has("image")) | .image |select (has("pullPolicy"))).pullPolicy = "Always"' $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR)/values.yaml
	yq e -i '(.* | select(has("liquibase")) | .liquibase.image).pullPolicy = "Always"' $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR)/values.yaml

	cd $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && \
		helm dep up && \
		helm upgrade ${PREVIEW_NAME} . \
			--install \
			--set global.application.name=default-app \
			--set global.keycloak.clientSecret=$(shell uuidgen) \
			--set global.gateway.http=false \
			--set global.gateway.domain=${GLOBAL_GATEWAY_DOMAIN} \
			--values $(MESSAGING_BROKER)-values.yaml \
			--values $(MESSAGING_PARTITIONED)-values.yaml \
			--values $(MESSAGING_DESTINATIONS)-values.yaml \
			--namespace ${PREVIEW_NAME} \
			--create-namespace \
			--atomic \
			--wait \
			--timeout 8m

delete:
	helm uninstall ${PREVIEW_NAME} --namespace ${PREVIEW_NAME} || echo "try to remove helm chart"
	kubectl delete ns ${PREVIEW_NAME} || echo "try to remove namespace ${PREVIEW_NAME}"

clone-chart:
	rm -rf $(ACTIVITI_CLOUD_FULL_CHART_CHECKOUT_DIR) && \
		git clone https://${GITHUB_TOKEN}@github.com/Activiti/activiti-cloud-full-chart.git \
			--branch $(ACTIVITI_CLOUD_FULL_CHART_RELEASE_BRANCH) \
			$(ACTIVITI_CLOUD_FULL_CHART_CHECKOUT_DIR) \
			--depth 1

create-pr: update-chart
	cd $(ACTIVITI_CLOUD_FULL_CHART_CHECKOUT_DIR) && \
		(git push -q origin :$(ACTIVITI_CLOUD_FULL_CHART_BRANCH) || true) && \
	  git checkout -q -b $(ACTIVITI_CLOUD_FULL_CHART_BRANCH) && \
		helm-docs && \
		git diff && \
		git commit -am "Update 'activiti-cloud-application' dependency to $(RELEASE_VERSION)" && \
		git push -qu origin $(ACTIVITI_CLOUD_FULL_CHART_BRANCH) && \
		gh pr create --fill --head $(ACTIVITI_CLOUD_FULL_CHART_BRANCH) --label updatebot ${GH_PR_CREATE_OPTS}

update-chart: clone-chart
	cd $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && \
		env VERSION=$(RELEASE_VERSION) make version && \
		env BACKEND_VERSION=$(RELEASE_VERSION) make update-docker-images

release: update-chart
	echo "RELEASE_VERSION: $(RELEASE_VERSION)"
	cd $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && \
    helm dep up && \
    helm lint && \
    cat Chart.yaml && \
	  cat values.yaml && \
	  ls charts -la

mvn/%:
	$(eval MODULE=$(word 1, $(subst mvn/, ,$@)))
	cd $(MODULE) &&	mvn ${MAVEN_CLI_OPTS} verify

docker/%:
	$(eval MODULE=$(word 1, $(subst docker/, ,$@)))

	@echo "Building docker image for $(MODULE):$(RELEASE_VERSION)..."
	cd activiti-cloud-examples && docker build -f $(MODULE)/Dockerfile -q -t docker.io/activiti/$(MODULE):$(RELEASE_VERSION) $(MODULE)
	docker push docker.io/activiti/$(MODULE):$(RELEASE_VERSION)

# follow instructions at https://github.com/docker/hub-feedback/issues/496#issuecomment-277562292
docker-delete/%:
	$(eval MODULE=$(word 2, $(subst /, ,$@)))

	@echo "Retrieve token"
	$(eval TOKEN=$(shell curl --silent --show-error --fail \
		-X POST \
		-H "Content-Type: application/json" \
		-H "Accept: application/json" \
		-d '{"username":"${DOCKERHUB_USERNAME}","password":"${DOCKERHUB_PASSWORD}"}' \
		https://hub.docker.com/v2/users/login/ | jq ".token"))
	$(eval URL=https://hub.docker.com/v2/repositories/activiti/$(MODULE)/tags/$(RELEASE_VERSION))
	@echo "Delete image from Docker Hub at $(URL)"
	$(eval HTTP_CODE=$(shell curl --silent --show-error --fail \
		--write-out "%{http_code}" \
		-X DELETE \
		-H "Authorization: JWT $(TOKEN)" \
		$(URL)))
	$(eval e=0)
	@if [ $(HTTP_CODE) -ne 200 ]; then \
		e=1; \
	fi
	@if [ $(HTTP_CODE) -eq 404 ]; then \
		echo "::warning title=Image not found::Image not found: $(MODULE):$(RELEASE_VERSION)"; \
		e=0; \
	fi
	@echo "Delete token"
	@curl --silent --show-error --fail \
		-X POST \
		-H "Accept: application/json" \
		-H "Authorization: JWT $(TOKEN)" \
		https://hub.docker.com/v2/logout/; \
	exit $$e

docker-delete-all: docker-delete/example-runtime-bundle docker-delete/activiti-cloud-query \
	docker-delete/example-cloud-connector docker-delete/activiti-cloud-modeling

version:
	mvn ${MAVEN_CLI_OPTS} versions:set -DprocessAllModules=true -DgenerateBackupPoms=false -DnewVersion=$(RELEASE_VERSION)

deploy:
	mvn ${MAVEN_CLI_OPTS} deploy -DskipTests

tag:
	git add -u
	git commit -m "Release $(RELEASE_VERSION)" --allow-empty
	git tag -fa $(RELEASE_VERSION) -m "Release version $(RELEASE_VERSION)"
	git push -f -q origin $(RELEASE_VERSION)

test/%:
	$(eval MODULE=$(word 2, $(subst /, ,$@)))

	mvn ${MAVEN_CLI_OPTS} -pl activiti-cloud-acceptance-scenarios/$(MODULE) -Droot.log.level=off verify -am

promote: version tag deploy updatebot/push-version
