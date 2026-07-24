package com.cyancoder.aiorchestrator.domain;

import java.util.ArrayList;
import java.util.List;

public class PlatformAppDslDefinition {
    private AppDescriptor app = new AppDescriptor();
    private List<EntityBlueprint> entities = new ArrayList<>();
    private List<RouteBlueprint> routes = new ArrayList<>();
    private List<FlowBlueprint> flows = new ArrayList<>();
    private List<PlatformResourceBlueprint> resources = new ArrayList<>();
    private DeliveryBlueprint delivery = new DeliveryBlueprint();
    private List<String> manualActions = new ArrayList<>();

    public AppDescriptor getApp() { return app; }
    public void setApp(AppDescriptor app) { this.app = app; }
    public List<EntityBlueprint> getEntities() { return entities; }
    public void setEntities(List<EntityBlueprint> entities) { this.entities = entities == null ? new ArrayList<>() : new ArrayList<>(entities); }
    public List<RouteBlueprint> getRoutes() { return routes; }
    public void setRoutes(List<RouteBlueprint> routes) { this.routes = routes == null ? new ArrayList<>() : new ArrayList<>(routes); }
    public List<FlowBlueprint> getFlows() { return flows; }
    public void setFlows(List<FlowBlueprint> flows) { this.flows = flows == null ? new ArrayList<>() : new ArrayList<>(flows); }
    public List<PlatformResourceBlueprint> getResources() { return resources; }
    public void setResources(List<PlatformResourceBlueprint> resources) {
        this.resources = resources == null ? new ArrayList<>() : new ArrayList<>(resources);
    }
    public DeliveryBlueprint getDelivery() { return delivery; }
    public void setDelivery(DeliveryBlueprint delivery) { this.delivery = delivery; }
    public List<String> getManualActions() { return manualActions; }
    public void setManualActions(List<String> manualActions) { this.manualActions = manualActions == null ? new ArrayList<>() : new ArrayList<>(manualActions); }
}
