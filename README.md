# Spring PetClinic sample application for Kubernetes

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The famous [Spring PetClinic sample application](https://github.com/spring-projects/spring-petclinic)
is now available as a Kubernetes native application leveraging microservices.

All dependencies / code which are not required to run this application with Kubernetes have been removed:
for example you don't need to use service discovery with 
[Spring Cloud Netflix](https://spring.io/projects/spring-cloud-netflix) since the Kubernetes platform
already provides one such implementation.

Moreover, container images for the Spring PetClinic are now built using [Cloud Native Buildpacks](https://buildpacks.io).
You will not find any `Dockerfile` in this repository: the `pack` CLI is used to build secure and
optimized container images for you.

## Building this application

You need a JDK 8+ to build this application:

```bash
$ ./mvnw clean package
```

Pre-built container images are available, so that you can start deploying this app to your favorite Kubernetes cluster. In case you'd like to build your own images, please follow these instructions.

[Read this guide](https://buildpacks.io/docs/install-pack/) to deploy the `pack` CLI to your workstation.

Many buildpack implementations are available: for best results, use [Paketo buildpacks](https://paketo.io):

```bash
$ pack set-default-builder gcr.io/paketo-buildpacks/builder:base
```

You're ready to build container images with no Dockerfile!

Use the provided `Makefile` to build container images:

```bash
$ make all DOCKER_PREFIX=myrepo
```

## Running this application locally

There is no need to run an Eureka server or anything else: this application is ready to run on your workstation.

Start the gateway:

```bash
$ java -jar spring-petclinic-api-gateway/target/spring-petclinic-api-gateway-VERSION.jar
```

Start the `customers` service:

```bash
java -jar spring-petclinic-customers-service/target/spring-petclinic-customers-service-VERSION.jar
```

Start the `vets` service:

```bash
$ java -jar spring-petclinic-vets-service/target/spring-petclinic-vets-service-VERSION.jar
```

Start the `visits` service:

```bash
$ java -jar spring-petclinic-visits-service/target/spring-petclinic-visits-service-VERSION.jar
```

Using your browser, go to http://localhost:8080 to access the application.

## Enabling Wavefront when running locally

This application includes Wavefront integration.
Enable profile `wavefront` to get access to a free Wavefront trial, in order to get metrics / traces from the application.

Set the system property `spring.profiles.active=wavefront` to enable `wavefront` profile.

For example:

```bash
$ java -jar -Dspring.profiles.active=wavefront spring-petclinic-visits-service/target/spring-petclinic-visits-service-VERSION.jar
```

As the application starts, a link to your application dashboard will be displayed in the console output:

```
Your existing Wavefront account information has been restored from disk.

To share this account, make sure the following is added to your configuration:

	management.metrics.export.wavefront.api-token=6fe0383b-0338-4664-883e-2642824b968c
	management.metrics.export.wavefront.uri=https://wavefront.surf
```

Use this link to get access to metrics / traces.

## Deploying this application to Kubernetes

This application relies on a MySQL database to persist data: you first need to deploy a MySQL instance.

Run these commands to create a MySQL database:

```bash
$ kubectl create ns spring-petclinic
$ helm upgrade pdb bitnami/mysql -n spring-petclinic -f k8s/services/mysql/values.yml --version 6.14.4 --install
```

If you want to enable Wavefront integration, you need to deploy a proxy first. [Follow this guide](https://docs.wavefront.com/kubernetes.html)
to deploy a Wavefront proxy for Kubernetes.

You may want to reuse this Wavefront proxy configuration:

```bash
$ kubectl create ns wavefront
$ helm upgrade wavefront wavefront/wavefront -f k8s/services/wavefront/values.yml --set wavefront.url=https://vmware.wavefront.com --set wavefront.token=wavefront-api-token --set clusterName=k8s-cluster-name -n wavefront --install --version 1.2.6
```

Make sure you set the Wavefront API token and the Kubernetes cluster name.

Edit file `k8s/wavefront/configmap.yml` and set the Kubernetes cluster name:

```yaml
data:
  # Don't forget to reuse the same K8s cluster name used by the Wavefront proxy.
  WAVEFRONT_APPLICATION_CLUSTER: dev01
```

Deploy this configuration file to your cluster:

```bash
$ kubectl apply -f k8s/wavefront
```

It's time to bind the application to your Wavefront space.
Create a `Secret` by setting the Wavefront API token:

```bash
$ kubectl -n spring-petclinic create secret generic app-wavefront --from-literal=MANAGEMENT_METRICS_EXPORT_WAVEFRONT_API-TOKEN=wavefront-api-token
```

You're almost there!

Deploy the application to your cluster:

```bash
$ kubectl apply -f k8s
```

The application is not publicly accessible: you need to create a Kubernetes service. Depending on your cluster configuration, you may have to use an `Ingress` route or a `LoadBalancer` to expose your application.

Run this command to use an ingress route (edit file `k8s/ingress/ingress.yml` first to set the route):

```bash
$ kubectl apply -f k8s/ingress
```

Run this command to use a Kubernetes managed load balancer:
```bash
$ kubectl apply -f k8s/loadbalancer
```

Congratulations: you're done!

![Spring Petclinic Microservices screenshot](docs/application-screenshot.png)
