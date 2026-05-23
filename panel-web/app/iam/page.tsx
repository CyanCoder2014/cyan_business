"use client";

import { useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import {
  assignIamClientRole,
  assignIamRealmRole,
  createIamUser,
  listIamClients,
  listIamClientRoles,
  listIamMemberships,
  listIamRealms,
  listIamRealmRoles,
  listIamUsers,
  resolveIamAccess,
  upsertIamClient,
  upsertIamClientRole,
  upsertIamMembership,
  upsertIamRealm,
  upsertIamRealmRole
} from "@/lib/service-api";
import type { ClientSummary, IamUserAccessSummary, RealmSummary, RoleCatalogSummary, UserSummary } from "@/lib/types";

function csv(value: string) {
  return value.split(",").map((item) => item.trim()).filter(Boolean);
}

export default function IamPage() {
  const [realmKey, setRealmKey] = useState("cyan");
  const [realmName, setRealmName] = useState("Cyan Realm");
  const [realmDescription, setRealmDescription] = useState("Default local realm");
  const [clientId, setClientId] = useState("cyan-panel");
  const [clientName, setClientName] = useState("Cyan Panel");
  const [redirectUris, setRedirectUris] = useState("http://localhost:3000/*");
  const [realmRoleKey, setRealmRoleKey] = useState("realm-user");
  const [clientRoleKey, setClientRoleKey] = useState("panel-operator");
  const [permissions, setPermissions] = useState("panel:read,builder:use");
  const [username, setUsername] = useState("cyan-user");
  const [password, setPassword] = useState("user123");
  const [email, setEmail] = useState("user@cyan.local");
  const [phoneNumber, setPhoneNumber] = useState("09121111111");
  const [realms, setRealms] = useState<RealmSummary[]>([]);
  const [clients, setClients] = useState<ClientSummary[]>([]);
  const [realmRoles, setRealmRoles] = useState<RoleCatalogSummary[]>([]);
  const [clientRoles, setClientRoles] = useState<RoleCatalogSummary[]>([]);
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [memberships, setMemberships] = useState<Array<{ username: string; realmKey: string; active: boolean; defaultRealm: boolean }>>([]);
  const [access, setAccess] = useState<IamUserAccessSummary | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function refresh() {
    const [realmItems, clientItems, membershipItems, userItems] = await Promise.all([
      listIamRealms(),
      listIamClients(),
      listIamMemberships(),
      listIamUsers()
    ]);
    setRealms(realmItems);
    setClients(clientItems);
    if (realmKey) {
      setRealmRoles(await listIamRealmRoles(realmKey).catch(() => []));
    }
    if (clientId) {
      setClientRoles(await listIamClientRoles(clientId).catch(() => []));
    }
    setMemberships(membershipItems);
    setUsers(userItems);
  }

  useEffect(() => {
    refresh().catch((error) => setStatus(error instanceof Error ? error.message : "Failed to load IAM workspace"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function run(action: () => Promise<void>, success: string) {
    setLoading(true);
    setStatus(null);
    try {
      await action();
      await refresh();
      setStatus(success);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "IAM action failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell title="IAM Admin" subtitle="Manage realms, clients, memberships, role catalogs, and per-client access in the custom SSO stack.">
      <div className="studio-grid">
        <section className="panel rail">
          <div className="form-grid">
            <div className="result-card">
              <h4>Realm</h4>
              <div className="field-grid">
                <div className="field">
                  <label>Realm key</label>
                  <input value={realmKey} onChange={(event) => setRealmKey(event.target.value)} />
                </div>
                <div className="field">
                  <label>Display name</label>
                  <input value={realmName} onChange={(event) => setRealmName(event.target.value)} />
                </div>
              </div>
              <div className="field">
                <label>Description</label>
                <textarea value={realmDescription} onChange={(event) => setRealmDescription(event.target.value)} />
              </div>
              <button type="button" className="btn" onClick={() => run(() => upsertIamRealm({ realmKey, displayName: realmName, description: realmDescription, active: true }).then(() => undefined), `Realm ${realmKey} saved.`)} disabled={loading}>Save realm</button>
            </div>

            <div className="result-card">
              <h4>Client</h4>
              <div className="field-grid">
                <div className="field">
                  <label>Client id</label>
                  <input value={clientId} onChange={(event) => setClientId(event.target.value)} />
                </div>
                <div className="field">
                  <label>Display name</label>
                  <input value={clientName} onChange={(event) => setClientName(event.target.value)} />
                </div>
              </div>
              <div className="field">
                <label>Redirect URIs CSV</label>
                <input value={redirectUris} onChange={(event) => setRedirectUris(event.target.value)} />
              </div>
              <button type="button" className="btn" onClick={() => run(() => upsertIamClient({ clientId, realmKey, displayName: clientName, description: `${clientName} client`, active: true, publicClient: true, redirectUris: csv(redirectUris) }).then(() => undefined), `Client ${clientId} saved.`)} disabled={loading}>Save client</button>
            </div>

            <div className="result-card">
              <h4>Realm role catalog</h4>
              <div className="field-grid">
                <div className="field">
                  <label>Role key</label>
                  <input value={realmRoleKey} onChange={(event) => setRealmRoleKey(event.target.value)} />
                </div>
                <div className="field">
                  <label>Permissions CSV</label>
                  <input value={permissions} onChange={(event) => setPermissions(event.target.value)} />
                </div>
              </div>
              <button type="button" className="btn" onClick={() => run(() => upsertIamRealmRole({ scopeType: "REALM", scopeKey: realmKey, roleKey: realmRoleKey, displayName: realmRoleKey, description: realmRoleKey, active: true, permissions: csv(permissions) }).then(() => undefined), `Realm role ${realmRoleKey} saved.`)} disabled={loading}>Save realm role</button>
            </div>

            <div className="result-card">
              <h4>Client role catalog</h4>
              <div className="field-grid">
                <div className="field">
                  <label>Role key</label>
                  <input value={clientRoleKey} onChange={(event) => setClientRoleKey(event.target.value)} />
                </div>
                <div className="field">
                  <label>Permissions CSV</label>
                  <input value={permissions} onChange={(event) => setPermissions(event.target.value)} />
                </div>
              </div>
              <button type="button" className="btn" onClick={() => run(() => upsertIamClientRole({ scopeType: "CLIENT", scopeKey: clientId, roleKey: clientRoleKey, displayName: clientRoleKey, description: clientRoleKey, active: true, permissions: csv(permissions) }).then(() => undefined), `Client role ${clientRoleKey} saved.`)} disabled={loading}>Save client role</button>
            </div>

            <div className="result-card">
              <h4>User directory</h4>
              <div className="field-grid">
                <div className="field">
                  <label>Username</label>
                  <input value={username} onChange={(event) => setUsername(event.target.value)} />
                </div>
                <div className="field">
                  <label>Password</label>
                  <input value={password} onChange={(event) => setPassword(event.target.value)} />
                </div>
              </div>
              <div className="field-grid">
                <div className="field">
                  <label>Email</label>
                  <input value={email} onChange={(event) => setEmail(event.target.value)} />
                </div>
                <div className="field">
                  <label>Phone number</label>
                  <input value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} />
                </div>
              </div>
              <button type="button" className="btn" onClick={() => run(() => createIamUser({ username, password, email, phoneNumber, mfaEnabled: false, roles: ["user"] }).then(() => undefined), `User ${username} created.`)} disabled={loading}>Create user</button>
            </div>

            <div className="result-card">
              <h4>User membership and assignments</h4>
              <div className="field"><label>Username</label><input value={username} onChange={(event) => setUsername(event.target.value)} /></div>
              <div className="hero-actions">
                <button type="button" className="btn" onClick={() => run(() => upsertIamMembership({ username, realmKey, active: true, defaultRealm: true }).then(() => undefined), `Membership saved for ${username}.`)} disabled={loading}>Add to realm</button>
                <button type="button" className="ghost-btn" onClick={() => run(() => assignIamRealmRole(realmKey, { username, roleKey: realmRoleKey }).then(() => undefined), `Assigned realm role ${realmRoleKey}.`)} disabled={loading}>Assign realm role</button>
                <button type="button" className="ghost-btn" onClick={() => run(() => assignIamClientRole(clientId, { username, roleKey: clientRoleKey }).then(() => undefined), `Assigned client role ${clientRoleKey}.`)} disabled={loading}>Assign client role</button>
                <button type="button" className="ghost-btn" onClick={() => run(() => resolveIamAccess(username, clientId).then((result) => setAccess(result)), `Resolved access for ${username}.`)} disabled={loading}>Resolve access</button>
              </div>
            </div>

            {status ? <div className="ai-banner">{status}</div> : null}
          </div>
        </section>

        <aside className="sidebar">
          <section className="panel rail"><p className="section-title">Realms</p><pre className="json-view">{JSON.stringify(realms, null, 2)}</pre></section>
          <section className="panel rail"><p className="section-title">Clients</p><pre className="json-view">{JSON.stringify(clients, null, 2)}</pre></section>
          <section className="panel rail"><p className="section-title">Realm roles</p><pre className="json-view">{JSON.stringify(realmRoles, null, 2)}</pre></section>
          <section className="panel rail"><p className="section-title">Client roles</p><pre className="json-view">{JSON.stringify(clientRoles, null, 2)}</pre></section>
          <section className="panel rail"><p className="section-title">Memberships</p><pre className="json-view">{JSON.stringify(memberships, null, 2)}</pre></section>
          <section className="panel rail"><p className="section-title">Users</p><pre className="json-view">{JSON.stringify(users, null, 2)}</pre></section>
          <section className="panel rail"><p className="section-title">Resolved access</p><pre className="json-view">{JSON.stringify(access, null, 2)}</pre></section>
        </aside>
      </div>
    </AppShell>
  );
}
