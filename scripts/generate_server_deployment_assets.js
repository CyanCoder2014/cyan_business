const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");

const services = [
  ["tax-pay-sys", "tax-pay-sys", 8002],
  ["factor-service", "factor-service", 8003],
  ["buyer-service", "buyer-service", 8004],
  ["product-service", "product-service", 8005],
  ["client-service", "client-service", 8010],
  ["sso-auth-service", "sso-auth-service", 9001],
  ["sso-user-service", "sso-user-service", 9002],
  ["sso-captcha-service", "sso-captcha-service", 9003],
  ["sso-otp-service", "sso-otp-service", 9004],
  ["sso-session-service", "sso-session-service", 9005],
  ["sso-fido-service", "sso-fido-service", 9006],
  ["content-service", "content-service", 9101],
  ["catalog-service", "catalog-service", 9102],
  ["crm-service", "crm-service", 9103],
  ["commerce-service", "commerce-service", 9104],
  ["finance-service", "finance-service", 9105],
  ["inventory-service", "inventory-service", 9106],
  ["report-service", "report-service", 9107],
  ["processor-service", "processor-service", 9108],
  ["event-service", "event-service", 9109],
  ["crm-automation-service", "crm-automation-service", 9110],
  ["finance-automation-service", "finance-automation-service", 9111],
  ["inventory-automation-service", "inventory-automation-service", 9112],
  ["report-automation-service", "report-automation-service", 9113],
  ["payment-service", "payment-service", 9114],
  ["storefront-service", "storefront-service", 9115],
  ["media-service", "media-service", 9116],
  ["cart-service", "cart-service", 9117],
  ["checkout-service", "checkout-service", 9118],
  ["bpm-service", "bpm-service", 9119],
  ["automation-orchestrator-service", "automation-orchestrator-service", 9120],
  ["ai-orchestrator-service", "ai-orchestrator-service", 9121],
  ["notification-service", "notification-service", 9122],
  ["payment-orchestrator-service", "payment-orchestrator-service", 9123],
  ["pricing-promotion-service", "pricing-promotion-service", 9124],
  ["search-index-service", "search-index-service", 9125],
  ["bot-adapter-service", "bot-adapter-service", 9126],
];

const serviceByName = Object.fromEntries(services.map((service) => [service[1], service]));
const aliasTargets = {
  "tax-service": ["tax-pay-sys", "tax-pay-sys", 8002],
};

const discoveryNeeds = new Map([
  ["sso-auth-service", ["sso-user-service", "sso-session-service", "sso-otp-service", "sso-captcha-service", "sso-fido-service"]],
  ["report-service", ["content-service", "catalog-service", "crm-service", "commerce-service", "finance-service", "inventory-service"]],
  ["storefront-service", ["content-service", "catalog-service", "media-service"]],
  ["checkout-service", ["cart-service", "pricing-promotion-service", "payment-orchestrator-service", "notification-service"]],
  ["payment-orchestrator-service", ["payment-service"]],
  [
    "bpm-service",
    [
      "automation-orchestrator-service",
      "notification-service",
      "content-service",
      "catalog-service",
      "crm-service",
      "commerce-service",
      "finance-service",
      "inventory-service",
      "report-service",
      "storefront-service",
      "media-service",
      "cart-service",
      "checkout-service",
      "payment-orchestrator-service",
      "pricing-promotion-service",
      "search-index-service",
    ],
  ],
  [
    "automation-orchestrator-service",
    [
      "notification-service",
      "content-service",
      "catalog-service",
      "crm-service",
      "commerce-service",
      "finance-service",
      "inventory-service",
      "report-service",
      "storefront-service",
      "media-service",
      "cart-service",
      "checkout-service",
      "payment-orchestrator-service",
      "pricing-promotion-service",
      "search-index-service",
      "bpm-service",
    ],
  ],
  [
    "ai-orchestrator-service",
    [
      "content-service",
      "catalog-service",
      "crm-service",
      "commerce-service",
      "finance-service",
      "inventory-service",
      "report-service",
      "storefront-service",
      "media-service",
      "cart-service",
      "checkout-service",
      "payment-orchestrator-service",
      "pricing-promotion-service",
      "search-index-service",
      "notification-service",
      "bpm-service",
    ],
  ],
  ["search-index-service", ["content-service", "catalog-service", "crm-service", "commerce-service", "finance-service", "inventory-service", "storefront-service", "media-service"]],
  ["factor-service", ["tax-service"]],
]);

const postgresServices = new Map([
  ["tax-pay-sys", "tax_pay_sys"],
  ["factor-service", "factor_service"],
  ["buyer-service", "buyer_service"],
  ["product-service", "product_service"],
  ["client-service", "client_service"],
  ["sso-auth-service", "sso_auth_service"],
  ["sso-user-service", "sso_user_service"],
  ["sso-otp-service", "sso_otp_service"],
  ["sso-session-service", "sso_session_service"],
  ["content-service", "content_service"],
  ["catalog-service", "catalog_service"],
  ["crm-service", "crm_service"],
  ["commerce-service", "commerce_service"],
  ["finance-service", "finance_service"],
  ["inventory-service", "inventory_service"],
  ["report-service", "report_service"],
  ["processor-service", "processor_service"],
  ["event-service", "event_service"],
  ["crm-automation-service", "crm_automation_service"],
  ["finance-automation-service", "finance_automation_service"],
  ["inventory-automation-service", "inventory_automation_service"],
  ["report-automation-service", "report_automation_service"],
  ["payment-service", "payment_service"],
  ["storefront-service", "storefront_service"],
  ["media-service", "media_service"],
  ["cart-service", "cart_service"],
  ["checkout-service", "checkout_service"],
  ["bpm-service", "bpm_service"],
  ["notification-service", "notification_service"],
  ["payment-orchestrator-service", "payment_orchestrator_service"],
  ["pricing-promotion-service", "pricing_promotion_service"],
  ["search-index-service", "search_index_service"],
]);

const mongoServices = new Map([
  ["tax-pay-sys", "tax_pay_sys"],
  ["factor-service", "factor_service"],
  ["buyer-service", "buyer_service"],
  ["product-service", "product_service"],
  ["client-service", "client_service"],
  ["content-service", "content_service"],
  ["catalog-service", "catalog_service"],
  ["crm-service", "crm_service"],
  ["commerce-service", "commerce_service"],
  ["finance-service", "finance_service"],
  ["inventory-service", "inventory_service"],
  ["report-service", "report_service"],
  ["payment-orchestrator-service", "payment_orchestrator_service"],
  ["storefront-service", "storefront_service"],
  ["media-service", "media_service"],
  ["cart-service", "cart_service"],
  ["checkout-service", "checkout_service"],
  ["automation-orchestrator-service", "automation_orchestrator_service"],
  ["ai-orchestrator-service", "ai_orchestrator_service"],
  ["notification-service", "notification_service"],
  ["pricing-promotion-service", "pricing_promotion_service"],
  ["search-index-service", "search_index_service"],
  ["bpm-service", "bpm_service"],
  ["bot-adapter-service", "bot_adapter_service"],
]);

const jwtServices = new Set([
  "tax-pay-sys",
  "factor-service",
  "buyer-service",
  "product-service",
  "client-service",
  "sso-user-service",
  "content-service",
  "catalog-service",
  "crm-service",
  "commerce-service",
  "finance-service",
  "inventory-service",
  "report-service",
  "payment-service",
  "storefront-service",
  "media-service",
  "cart-service",
  "checkout-service",
  "bpm-service",
  "automation-orchestrator-service",
  "ai-orchestrator-service",
  "notification-service",
  "payment-orchestrator-service",
  "pricing-promotion-service",
  "search-index-service",
  "bot-adapter-service",
]);

const kafkaServices = new Set([
  "event-service",
  "crm-automation-service",
  "finance-automation-service",
  "inventory-automation-service",
  "report-automation-service",
  "notification-service",
]);

const processorEventServices = new Set([
  "content-service",
  "catalog-service",
  "crm-service",
  "commerce-service",
  "finance-service",
  "inventory-service",
]);

const internalServices = new Map([
  ["tax-pay-sys", ["tax_internal", "tax_secret"]],
  ["factor-service", ["factor_internal", "factor_secret"]],
  ["buyer-service", ["buyer_internal", "buyer_secret"]],
  ["product-service", ["product_internal", "product_secret"]],
  ["client-service", ["client_internal", "client_secret"]],
  ["content-service", ["content_internal", "content_secret"]],
  ["catalog-service", ["catalog_internal", "catalog_secret"]],
  ["crm-service", ["crm_internal", "crm_secret"]],
  ["commerce-service", ["commerce_internal", "commerce_secret"]],
  ["finance-service", ["finance_internal", "finance_secret"]],
  ["inventory-service", ["inventory_internal", "inventory_secret"]],
  ["report-service", ["report_internal", "report_secret"]],
  ["payment-service", ["payment_internal", "payment_secret"]],
  ["storefront-service", ["storefront_internal", "storefront_secret"]],
  ["media-service", ["media_internal", "media_secret"]],
  ["cart-service", ["cart_internal", "cart_secret"]],
  ["checkout-service", ["checkout_internal", "checkout_secret"]],
  ["bpm-service", ["bpm_internal", "bpm_secret"]],
  ["automation-orchestrator-service", ["automation_orchestrator_internal", "automation_orchestrator_secret"]],
  ["ai-orchestrator-service", ["ai_orchestrator_internal", "ai_orchestrator_secret"]],
  ["notification-service", ["notification_internal", "notification_secret"]],
  ["payment-orchestrator-service", ["payment_orchestrator_internal", "payment_orchestrator_secret"]],
  ["pricing-promotion-service", ["pricing_promotion_internal", "pricing_promotion_secret"]],
  ["search-index-service", ["search_index_internal", "search_index_secret"]],
]);

const axonServices = new Set(["tax-pay-sys", "factor-service", "buyer-service", "product-service", "client-service"]);

function writeFile(relativePath, content) {
  const target = path.join(root, relativePath);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.writeFileSync(target, content);
}

function springPlaceholder(name, fallback) {
  return "${" + name + ":" + fallback + "}";
}

function envPrefix(appName) {
  return appName.toUpperCase().replace(/-/g, "_");
}

function serverProfile(appName) {
  const prefix = envPrefix(appName);
  const lines = [
    "# Server deployment profile for Kubernetes service discovery and Envoy Gateway ingress.",
    "eureka.client.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false",
  ];

  for (const key of discoveryNeeds.get(appName) || []) {
    const target = serviceByName[key] || aliasTargets[key];
    if (target) {
      lines.push(`spring.cloud.discovery.client.simple.instances.${key}[0].uri=http://${target[1]}:${target[2]}`);
    }
  }

  if (postgresServices.has(appName)) {
    const db = postgresServices.get(appName);
    lines.push(`spring.datasource.url=${springPlaceholder(`${prefix}_DATASOURCE_URL`, `jdbc:postgresql://postgres:5432/${db}`)}`);
    lines.push("spring.datasource.driverClassName=org.postgresql.Driver");
    lines.push(`spring.datasource.username=${springPlaceholder(`${prefix}_DATASOURCE_USERNAME`, springPlaceholder("SPRING_DATASOURCE_USERNAME", "postgres"))}`);
    lines.push(`spring.datasource.password=${springPlaceholder(`${prefix}_DATASOURCE_PASSWORD`, springPlaceholder("SPRING_DATASOURCE_PASSWORD", "postgres"))}`);
  }

  if (mongoServices.has(appName)) {
    const db = mongoServices.get(appName);
    lines.push(`spring.data.mongodb.uri=${springPlaceholder(`${prefix}_MONGODB_URI`, `mongodb://mongo:27017/${db}`)}`);
  }

  if (jwtServices.has(appName)) {
    lines.push(`spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${springPlaceholder("JWKS_URI", "http://sso-auth-service:9001/.well-known/jwks.json")}`);
  }

  if (kafkaServices.has(appName)) {
    lines.push(`spring.kafka.bootstrap-servers=${springPlaceholder("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")}`);
  }

  if (processorEventServices.has(appName)) {
    lines.push(`processor.service.base-url=${springPlaceholder("PROCESSOR_SERVICE_BASE_URL", "http://processor-service:9108")}`);
    lines.push(`event.service.base-url=${springPlaceholder("EVENT_SERVICE_BASE_URL", "http://event-service:9109")}`);
  }

  if (internalServices.has(appName)) {
    const [username, password] = internalServices.get(appName);
    lines.push(`service.internal.username=${springPlaceholder(`${prefix}_INTERNAL_USERNAME`, username)}`);
    lines.push(`service.internal.password=${springPlaceholder(`${prefix}_INTERNAL_PASSWORD`, password)}`);
  }

  if (appName === "sso-auth-service") {
    lines.push(`sso.jwt.issuer=${springPlaceholder("SSO_JWT_ISSUER", "https://api.example.com")}`);
    lines.push(`sso.jwt.audience=${springPlaceholder("SSO_JWT_AUDIENCE", "cyan-business")}`);
  }

  if (axonServices.has(appName)) {
    lines.push(`axon.axonserver.servers=${springPlaceholder("AXON_SERVER_SERVERS", "axon-server:8124")}`);
    lines.push(`axon.axonserver.token=${springPlaceholder("AXON_SERVER_TOKEN", "123cba")}`);
  }

  if (appName === "automation-orchestrator-service") {
    lines.push(`automation.callback.secret=${springPlaceholder("AUTOMATION_CALLBACK_SECRET", "change-me")}`);
  }

  if (appName === "ai-orchestrator-service") {
    lines.push(`llm.openai.api-key=${springPlaceholder("OPENAI_API_KEY", "")}`);
    lines.push(`llm.openrouter.api-key=${springPlaceholder("OPENROUTER_API_KEY", "")}`);
    lines.push(`llm.gapgpt.api-key=${springPlaceholder("GAPGPT_API_KEY", "")}`);
    lines.push(`llm.ollama.base-url=${springPlaceholder("OLLAMA_BASE_URL", "http://ollama:11434")}`);
  }

  if (appName === "bot-adapter-service") {
    lines.push(`ai-orchestrator.base-url=${springPlaceholder("AI_ORCHESTRATOR_BASE_URL", "http://ai-orchestrator-service:9121")}`);
    lines.push(`ai-orchestrator.public-base-url=${springPlaceholder("PUBLIC_PLATFORM_BASE_URL", "https://api.example.com")}`);
  }

  if (appName === "tax-pay-sys") {
    lines.push(`factor.service.base-url=${springPlaceholder("FACTOR_SERVICE_BASE_URL", "http://factor-service:8003")}`);
    lines.push(`client.service.base-url=${springPlaceholder("CLIENT_SERVICE_BASE_URL", "http://client-service:8010")}`);
    lines.push(`external.home.base-url=${springPlaceholder("EXTERNAL_HOME_BASE_URL", "http://localhost:8081")}`);
  }

  return lines.join("\n") + "\n";
}

function dockerfile(port) {
  return [
    "FROM eclipse-temurin:25-jre",
    "WORKDIR /app",
    "COPY build/libs/*.jar app.jar",
    `EXPOSE ${port}`,
    "ENV SPRING_PROFILES_ACTIVE=server",
    'ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]',
    "",
  ].join("\n");
}

function appsYaml() {
  return services
    .map(([, appName, port]) => {
      return [
        "apiVersion: apps/v1",
        "kind: Deployment",
        "metadata:",
        `  name: ${appName}`,
        "  labels:",
        `    app.kubernetes.io/name: ${appName}`,
        "    app.kubernetes.io/part-of: cyan-business",
        "spec:",
        "  replicas: 1",
        "  selector:",
        "    matchLabels:",
        `      app.kubernetes.io/name: ${appName}`,
        "  template:",
        "    metadata:",
        "      labels:",
        `        app.kubernetes.io/name: ${appName}`,
        "        app.kubernetes.io/part-of: cyan-business",
        "    spec:",
        "      containers:",
        `        - name: ${appName}`,
        `          image: ghcr.io/your-org/cyan-business/${appName}:develop`,
        "          imagePullPolicy: IfNotPresent",
        "          ports:",
        "            - name: http",
        `              containerPort: ${port}`,
        "          env:",
        "            - name: SPRING_PROFILES_ACTIVE",
        "              value: server",
        "            - name: JAVA_TOOL_OPTIONS",
        "              value: -XX:MaxRAMPercentage=75.0",
        "          envFrom:",
        "            - secretRef:",
        "                name: cyan-platform-secrets",
        "                optional: true",
        "          readinessProbe:",
        "            tcpSocket:",
        "              port: http",
        "            initialDelaySeconds: 20",
        "            periodSeconds: 10",
        "          livenessProbe:",
        "            tcpSocket:",
        "              port: http",
        "            initialDelaySeconds: 60",
        "            periodSeconds: 20",
        "---",
        "apiVersion: v1",
        "kind: Service",
        "metadata:",
        `  name: ${appName}`,
        "  labels:",
        `    app.kubernetes.io/name: ${appName}`,
        "    app.kubernetes.io/part-of: cyan-business",
        "spec:",
        "  selector:",
        `    app.kubernetes.io/name: ${appName}`,
        "  ports:",
        "    - name: http",
        `      port: ${port}`,
        "      targetPort: http",
        "",
      ].join("\n");
    })
    .join("---\n");
}

const routeRules = [
  [["/v2/api/tax-service/", "/v2/api/tax/"], "tax-pay-sys", 8002],
  [["/v2/api/factor-service/"], "factor-service", 8003],
  [["/v2/api/buyer-service/"], "buyer-service", 8004],
  [["/v2/api/product-service/"], "product-service", 8005],
  [["/v2/api/client-service/"], "client-service", 8010],
  [["/realms/", "/api/sso/auth/", "/.well-known/"], "sso-auth-service", 9001],
  [["/api/sso/users/", "/api/sso/iam/"], "sso-user-service", 9002],
  [["/api/sso/captcha/"], "sso-captcha-service", 9003],
  [["/api/sso/otp/"], "sso-otp-service", 9004],
  [["/api/sso/sessions/"], "sso-session-service", 9005],
  [["/api/sso/fido/"], "sso-fido-service", 9006],
  [["/api/content-service/"], "content-service", 9101],
  [["/api/catalog-service/"], "catalog-service", 9102],
  [["/api/crm-service/"], "crm-service", 9103],
  [["/api/commerce-service/"], "commerce-service", 9104],
  [["/api/finance-service/"], "finance-service", 9105],
  [["/api/inventory-service/"], "inventory-service", 9106],
  [["/api/report-service/"], "report-service", 9107],
  [["/api/processor-service/"], "processor-service", 9108],
  [["/api/event-service/"], "event-service", 9109],
  [["/api/crm-automation-service/"], "crm-automation-service", 9110],
  [["/api/finance-automation-service/"], "finance-automation-service", 9111],
  [["/api/inventory-automation-service/"], "inventory-automation-service", 9112],
  [["/api/report-automation-service/"], "report-automation-service", 9113],
  [["/api/payment-service/", "/endpoint/payment/", "/internal/payment/", "/public/payment/"], "payment-service", 9114],
  [["/api/storefront-service/", "/public/storefront/"], "storefront-service", 9115],
  [["/api/media-service/", "/public/media/", "/internal/media/"], "media-service", 9116],
  [["/api/cart-service/"], "cart-service", 9117],
  [["/api/checkout-service/"], "checkout-service", 9118],
  [["/api/bpm-service/", "/endpoint/bpm/", "/internal/bpm/", "/public/bpm/"], "bpm-service", 9119],
  [["/api/automation-orchestrator-service/", "/internal/automation-orchestrator/"], "automation-orchestrator-service", 9120],
  [["/api/ai-orchestrator-service/", "/endpoint/ai-orchestrator/", "/internal/ai-orchestrator/"], "ai-orchestrator-service", 9121],
  [["/api/notification-service/", "/endpoint/notifications/", "/internal/notifications/"], "notification-service", 9122],
  [["/api/payment-orchestrator-service/"], "payment-orchestrator-service", 9123],
  [["/api/pricing-promotion-service/"], "pricing-promotion-service", 9124],
  [["/api/search-index-service/", "/public/search-index/", "/internal/search-index/"], "search-index-service", 9125],
  [["/api/bot-adapter-service/", "/endpoint/bot-adapter/", "/public/bot-adapter/"], "bot-adapter-service", 9126],
];

function envoyGatewayYaml() {
  const maxRulesPerRoute = 16;
  const lines = [
    "apiVersion: gateway.networking.k8s.io/v1",
    "kind: GatewayClass",
    "metadata:",
    "  name: cyan-envoy",
    "spec:",
    "  controllerName: gateway.envoyproxy.io/gatewayclass-controller",
    "---",
    "apiVersion: gateway.networking.k8s.io/v1",
    "kind: Gateway",
    "metadata:",
    "  name: cyan-gateway",
    "spec:",
    "  gatewayClassName: cyan-envoy",
    "  listeners:",
    "    - name: http",
    "      protocol: HTTP",
    "      port: 80",
    "      allowedRoutes:",
    "        namespaces:",
    "          from: Same",
  ];

  for (let i = 0; i < routeRules.length; i += maxRulesPerRoute) {
    const chunk = routeRules.slice(i, i + maxRulesPerRoute);
    const routeName =
      i === 0 ? "cyan-platform-routes" : `cyan-platform-routes-${Math.floor(i / maxRulesPerRoute) + 1}`;

    lines.push("---");
    lines.push("apiVersion: gateway.networking.k8s.io/v1");
    lines.push("kind: HTTPRoute");
    lines.push("metadata:");
    lines.push(`  name: ${routeName}`);
    lines.push("spec:");
    lines.push("  parentRefs:");
    lines.push("    - name: cyan-gateway");
    lines.push("  rules:");

    for (const [prefixes, service, port] of chunk) {
      lines.push("    - matches:");
      for (const prefix of prefixes) {
        lines.push("        - path:");
        lines.push("            type: PathPrefix");
        lines.push(`            value: ${prefix}`);
      }
      lines.push("      backendRefs:");
      lines.push(`        - name: ${service}`);
      lines.push(`          port: ${port}`);
    }
  }

  return lines.join("\n") + "\n";
}

function secretTemplateYaml() {
  return [
    "apiVersion: v1",
    "kind: Secret",
    "metadata:",
    "  name: cyan-platform-secrets",
    "type: Opaque",
    "stringData:",
    "  SPRING_DATASOURCE_USERNAME: postgres",
    "  SPRING_DATASOURCE_PASSWORD: change-me",
    "  KAFKA_BOOTSTRAP_SERVERS: kafka:9092",
    "  JWKS_URI: http://sso-auth-service:9001/.well-known/jwks.json",
    "  SSO_JWT_ISSUER: https://api.example.com",
    "  SSO_JWT_AUDIENCE: cyan-business",
    "  PUBLIC_PLATFORM_BASE_URL: https://api.example.com",
    "  AXON_SERVER_SERVERS: axon-server:8124",
    "  AXON_SERVER_TOKEN: change-me",
    "  AUTOMATION_CALLBACK_SECRET: change-me",
    '  OPENAI_API_KEY: ""',
    '  OPENROUTER_API_KEY: ""',
    '  GAPGPT_API_KEY: ""',
    "  CONTENT_SERVICE_INTERNAL_PASSWORD: change-me",
    "  AI_ORCHESTRATOR_SERVICE_INTERNAL_PASSWORD: change-me",
    "  BPM_SERVICE_INTERNAL_PASSWORD: change-me",
    "  NOTIFICATION_SERVICE_INTERNAL_PASSWORD: change-me",
    "",
  ].join("\n");
}

function githubWorkflowYaml() {
  const serviceList = services.map(([, appName]) => appName).join(", ");
  const serviceShellList = services.map(([, appName]) => appName).join(" ");

  return [
    "name: Build and deploy Cyan Business",
    "",
    "on:",
    "  push:",
    "    branches:",
    "      - develop",
    "      - main",
    "",
    "permissions:",
    "  contents: read",
    "  packages: write",
    "",
    "env:",
    "  REGISTRY: ghcr.io/${{ github.repository_owner }}/cyan-business",
    "",
    "jobs:",
    "  build:",
    "    runs-on: ubuntu-latest",
    "    strategy:",
    "      fail-fast: false",
    "      matrix:",
    `        service: [${serviceList}]`,
    "    steps:",
    "      - uses: actions/checkout@v4",
    "      - uses: actions/setup-java@v4",
    "        with:",
    "          distribution: temurin",
    "          java-version: '25'",
    "      - uses: gradle/actions/setup-gradle@v4",
    "      - name: Build service jar",
    "        run: ./gradlew :${{ matrix.service }}:bootJar",
    "      - name: Log in to GitHub Container Registry",
    "        uses: docker/login-action@v3",
    "        with:",
    "          registry: ghcr.io",
    "          username: ${{ github.actor }}",
    "          password: ${{ secrets.GITHUB_TOKEN }}",
    "      - name: Build and push image",
    "        uses: docker/build-push-action@v6",
    "        with:",
    "          context: ./${{ matrix.service }}",
    "          push: true",
    "          tags: |",
    "            ${{ env.REGISTRY }}/${{ matrix.service }}:${{ github.ref_name }}-${{ github.sha }}",
    "            ${{ env.REGISTRY }}/${{ matrix.service }}:${{ github.ref_name }}",
    "",
    "  deploy:",
    "    runs-on: ubuntu-latest",
    "    needs: build",
    "    environment: ${{ github.ref_name == 'main' && 'production' || 'staging' }}",
    "    env:",
    "      KUBE_NAMESPACE: ${{ github.ref_name == 'main' && 'cyan-production' || 'cyan-staging' }}",
    "      IMAGE_TAG: ${{ github.ref_name }}-${{ github.sha }}",
    "    steps:",
    "      - uses: actions/checkout@v4",
    "      - name: Install kubectl",
    "        uses: azure/setup-kubectl@v4",
    "      - name: Configure kubeconfig",
    "        run: |",
    "          mkdir -p ~/.kube",
    '          echo "${KUBE_CONFIG}" | base64 -d > ~/.kube/config',
    "        env:",
    "          KUBE_CONFIG: ${{ secrets.KUBE_CONFIG }}",
    "      - name: Apply manifests",
    "        run: |",
    '          kubectl create namespace "${KUBE_NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f -',
    '          kubectl -n "${KUBE_NAMESPACE}" apply -k deploy/kubernetes',
    "      - name: Roll images",
    "        run: |",
    `          services="${serviceShellList}"`,
    "          for service in ${services}; do",
    '            kubectl -n "${KUBE_NAMESPACE}" set image deployment/${service} ${service}=${REGISTRY}/${service}:${IMAGE_TAG}',
    "          done",
    "      - name: Wait for rollout",
    "        run: |",
    `          services="${serviceShellList}"`,
    "          for service in ${services}; do",
    '            kubectl -n "${KUBE_NAMESPACE}" rollout status deployment/${service} --timeout=180s',
    "          done",
    "",
  ].join("\n");
}

function jenkinsfile() {
  const serviceShellList = services.map(([, appName]) => appName).join(" ");
  return [
    "pipeline {",
    "  agent any",
    "",
    "  options {",
    "    timestamps()",
    "    skipDefaultCheckout(false)",
    "  }",
    "",
    "  environment {",
    "    REGISTRY = credentials('cyan-container-registry')",
    "    REGISTRY_HOST = 'ghcr.io/your-org/cyan-business'",
    "    IMAGE_TAG = \"${env.BRANCH_NAME}-${env.GIT_COMMIT}\"",
    "    KUBE_NAMESPACE = \"${env.BRANCH_NAME == 'main' ? 'cyan-production' : 'cyan-staging'}\"",
    `    SERVICES = '${serviceShellList}'`,
    "  }",
    "",
    "  stages {",
    "    stage('Branch gate') {",
    "      when {",
    "        not { anyOf { branch 'develop'; branch 'main' } }",
    "      }",
    "      steps {",
    "        error('Automatic deployment is enabled only for develop and main branches.')",
    "      }",
    "    }",
    "",
    "    stage('Build jars') {",
    "      steps {",
    "        script {",
    "          def modules = env.SERVICES.split(' ').collect { \":${it}:bootJar\" }.join(' ')",
    "          sh \"./gradlew ${modules}\"",
    "        }",
    "      }",
    "    }",
    "",
    "    stage('Build and push images') {",
    "      steps {",
    "        sh '''",
    "          set -eu",
    '          echo "$REGISTRY_PSW" | docker login ghcr.io -u "$REGISTRY_USR" --password-stdin',
    "          for service in $SERVICES; do",
    '            docker build -t "$REGISTRY_HOST/$service:$IMAGE_TAG" -t "$REGISTRY_HOST/$service:$BRANCH_NAME" "$service"',
    '            docker push "$REGISTRY_HOST/$service:$IMAGE_TAG"',
    '            docker push "$REGISTRY_HOST/$service:$BRANCH_NAME"',
    "          done",
    "        '''",
    "      }",
    "    }",
    "",
    "    stage('Deploy') {",
    "      steps {",
    "        withCredentials([file(credentialsId: 'cyan-kubeconfig', variable: 'KUBECONFIG')]) {",
    "          sh '''",
    "            set -eu",
    '            kubectl create namespace "$KUBE_NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -',
    '            kubectl -n "$KUBE_NAMESPACE" apply -k deploy/kubernetes',
    "            for service in $SERVICES; do",
    '              kubectl -n "$KUBE_NAMESPACE" set image deployment/$service $service=$REGISTRY_HOST/$service:$IMAGE_TAG',
    '              kubectl -n "$KUBE_NAMESPACE" rollout status deployment/$service --timeout=180s',
    "            done",
    "          '''",
    "        }",
    "      }",
    "    }",
    "  }",
    "}",
    "",
  ].join("\n");
}

function kustomizationYaml() {
  return [
    "apiVersion: kustomize.config.k8s.io/v1beta1",
    "kind: Kustomization",
    "resources:",
    "  - apps.yaml",
    "  - envoy-gateway.yaml",
    "",
  ].join("\n");
}

for (const [dir, appName, port] of services) {
  writeFile(path.join(dir, "src/main/resources/application-server.properties"), serverProfile(appName));
  writeFile(path.join(dir, "Dockerfile"), dockerfile(port));
}

writeFile("deploy/kubernetes/apps.yaml", appsYaml());
writeFile("deploy/kubernetes/envoy-gateway.yaml", envoyGatewayYaml());
writeFile("deploy/kubernetes/secret.template.yaml", secretTemplateYaml());
writeFile("deploy/kubernetes/kustomization.yaml", kustomizationYaml());
writeFile(".github/workflows/deploy.yml", githubWorkflowYaml());
writeFile("Jenkinsfile", jenkinsfile());

console.log(`Generated server deployment assets for ${services.length} services.`);
