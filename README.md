# GitHub Activity Application

This project aims at centralizing all the information about the status of Quarkus QE activities in upstream.

## Setup
In your application directory, create a .env file containing your OAuth token:

```
ACTIVITY_TOKEN=<TOKEN>
```

The token only needs read access to the repository.

The application visualize the information about the Quarkus QE user.
For that reason, you need to pass the Quarkus QE member logins.
The application accepts these logins as runtime configuration property with a key `activity.logins`.
For example, you can set the logins with a system property when starting the application:

```bash
-Dactivity.logins=qe-user-name-1,qe-user-name-2
```

## Secure the application

By default, the application only grants access to authenticated users with role `quarkus-qe`.
Quarkus SecurityIdentity roles are mapped from access token claim `realm_access/roles`.
If you use this application locally, you can disable security with following configuration property:

```
activity.security.enabled=false
```

As always, you can set this configuration property as a system property like this:

```
-Dactivity.security.enabled=false
```

## Configure OIDC extension

Following 3 configuration properties must be set:

```
quarkus.oidc.auth-server-url=https://your-auth-server/auth/realms/your-realm
quarkus.oidc.client-id=your-client-id
quarkus.oidc.credentials.secret=your-client-secret
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
mvn compile quarkus:dev
```

By default, the security is disabled in DEV mode, however you can enable it like this:

```shell script
mvn compile quarkus:dev -Dactivity.security.enabled=true
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
mvn package
```
The application is now runnable using `ACTIVITY_TOKEN=your_token java -jar target/quarkus-app/quarkus-run.jar`.

## Creating a native executable
You can create a native executable using: 
```shell script
mvn package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
mvn package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `ACTIVITY_TOKEN=your_token ./target/gh-activity-1.0.0-SNAPSHOT-runner`

## Deploying to OpenShift
JVM build:
```shell
mvn clean package -Dquarkus.kubernetes.deploy=true -Dactivity.logins=user-name-1
```
Native build:
```shell
mvn clean package -Dquarkus.kubernetes.deploy=true -Dquarkus.native.container-build=true -Dnative -Dactivity.logins=user-name-1
```

OpenShift secret for GitHub API needs to be created beforehand.
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: gh-activity-token
stringData:
  ACTIVITY_TOKEN: your_token
```

Deploy the secret using `oc create -f secret.yaml` command, update it using `oc replace -f secret.yaml` command. 

If the `activity.security.enabled` configuration key is set to true, an OIDC client secret must be provided.
Please create the secret called `gh-activity-oidc-client-secret` using same steps as described above.
The `stringData` should look like this: 
```
OIDC_CLIENT_SECRET: your-client-secret
OIDC_CLIENT_ID: your-client-id
```

Please note that if you want OpenShift route to be exposed for you, just set `quarkus.openshift.route.expose=true` inside the `application.properties` file.
It's disabled by default as we create route with the `oc` command line tool.