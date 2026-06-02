pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(false)
  }

  environment {
    REGISTRY = credentials('cyan-container-registry')
    REGISTRY_HOST = 'ghcr.io/your-org/cyan-business'
    IMAGE_TAG = "${env.BRANCH_NAME}-${env.GIT_COMMIT}"
    KUBE_NAMESPACE = "${env.BRANCH_NAME == 'main' ? 'cyan-production' : 'cyan-staging'}"
    SERVICES = 'tax-pay-sys factor-service buyer-service product-service client-service sso-auth-service sso-user-service sso-captcha-service sso-otp-service sso-session-service sso-fido-service content-service catalog-service crm-service commerce-service finance-service inventory-service report-service processor-service event-service crm-automation-service finance-automation-service inventory-automation-service report-automation-service payment-service storefront-service media-service cart-service checkout-service bpm-service automation-orchestrator-service ai-orchestrator-service notification-service payment-orchestrator-service pricing-promotion-service search-index-service bot-adapter-service'
  }

  stages {
    stage('Branch gate') {
      when {
        not { anyOf { branch 'develop'; branch 'main' } }
      }
      steps {
        error('Automatic deployment is enabled only for develop and main branches.')
      }
    }

    stage('Build jars') {
      steps {
        script {
          def modules = env.SERVICES.split(' ').collect { ":${it}:bootJar" }.join(' ')
          sh "./gradlew ${modules}"
        }
      }
    }

    stage('Build and push images') {
      steps {
        sh '''
          set -eu
          echo "$REGISTRY_PSW" | docker login ghcr.io -u "$REGISTRY_USR" --password-stdin
          for service in $SERVICES; do
            docker build -t "$REGISTRY_HOST/$service:$IMAGE_TAG" -t "$REGISTRY_HOST/$service:$BRANCH_NAME" "$service"
            docker push "$REGISTRY_HOST/$service:$IMAGE_TAG"
            docker push "$REGISTRY_HOST/$service:$BRANCH_NAME"
          done
        '''
      }
    }

    stage('Deploy') {
      steps {
        withCredentials([file(credentialsId: 'cyan-kubeconfig', variable: 'KUBECONFIG')]) {
          sh '''
            set -eu
            kubectl create namespace "$KUBE_NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
            kubectl -n "$KUBE_NAMESPACE" apply -k deploy/kubernetes
            for service in $SERVICES; do
              kubectl -n "$KUBE_NAMESPACE" set image deployment/$service $service=$REGISTRY_HOST/$service:$IMAGE_TAG
              kubectl -n "$KUBE_NAMESPACE" rollout status deployment/$service --timeout=180s
            done
          '''
        }
      }
    }
  }
}
