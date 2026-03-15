export async function getJson(url) {
  const response = await fetch(url);
  if (!response.ok) {
    const message = await extractErrorMessage(response);
    throw new Error(message);
  }
  return response.json();
}

export async function postJson(url, payload) {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    const error = await extractError(response);
    throw error;
  }
  return response.json();
}

export async function putJson(url, payload) {
  const response = await fetch(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    const error = await extractError(response);
    throw error;
  }
  return response.json();
}

export async function deleteJson(url) {
  const response = await fetch(url, {
    method: "DELETE",
  });
  if (!response.ok) {
    const error = await extractError(response);
    throw error;
  }
  return response.json();
}

// 实例管理API
export async function getInstances(region, page = 1, size = 10) {
  return getJson(`/api/instances?region=${encodeURIComponent(region)}&page=${page}&size=${size}`);
}

export async function createInstance(request) {
  return postJson("/api/instances", request);
}

export async function startInstance(instanceId, region) {
  return postJson(`/api/instances/${instanceId}/start?region=${encodeURIComponent(region)}`, {});
}

export async function stopInstance(instanceId, region) {
  return postJson(`/api/instances/${instanceId}/stop?region=${encodeURIComponent(region)}`, {});
}

export async function rebootInstance(instanceId, region) {
  return postJson(`/api/instances/${instanceId}/reboot?region=${encodeURIComponent(region)}`, {});
}

export async function modifyInstance(instanceId, region, request) {
  return putJson(`/api/instances/${instanceId}?region=${encodeURIComponent(region)}`, request);
}

export async function destroyInstance(instanceId, region) {
  return deleteJson(`/api/instances/${instanceId}?region=${encodeURIComponent(region)}`);
}

export async function renewInstance(instanceId, region, periodMonths) {
  return postJson(`/api/instances/${instanceId}/renew?region=${encodeURIComponent(region)}&periodMonths=${periodMonths}`, {});
}

export async function getRegionInstanceCounts() {
  return getJson('/api/regions/instance-counts');
}

export async function getBalance() {
  return getJson("/api/billing/balance");
}

export async function getBills(page = 1, size = 10, billingCycle = null) {
  const params = new URLSearchParams({ page, size });
  if (billingCycle) params.append("billingCycle", billingCycle);
  return getJson(`/api/billing/bills?${params.toString()}`);
}

export async function getBillDetail(billId) {
  return getJson(`/api/billing/bills/${billId}`);
}

async function extractError(response) {
  const fallback = `Request failed: ${response.status}`;
  const contentType = response.headers.get("content-type") || "";
  try {
    if (contentType.includes("application/json")) {
      const data = await response.json();
      const message = data.message || data.error || fallback;
      const error = new Error(message);
      error.code = data.code;
      error.payload = data;
      return error;
    }
    const text = await response.text();
    return new Error(text || fallback);
  } catch (error) {
    return new Error(fallback);
  }
}

async function extractErrorMessage(response) {
  const fallback = `Request failed: ${response.status}`;
  try {
    const text = await response.text();
    return text || fallback;
  } catch (error) {
    return fallback;
  }
}
