const PREFIX = "cvm.cache.";
const DEFAULT_TTL_MS = 10 * 60 * 1000;

// Region cache constants
const REGION_CACHE_KEY = 'region.data';
const REGION_CACHE_TTL_MS = 24 * 60 * 60 * 1000; // 24小时
const REGION_COOKIE_TTL_MS = 7 * 24 * 60 * 60 * 1000; // 7天
const REGION_DATA_VERSION = '1.0';

export function cacheKey(key) {
  return `${PREFIX}${key}`;
}

export function writeCache(key, value) {
  const payload = { ts: Date.now(), value };
  localStorage.setItem(cacheKey(key), JSON.stringify(payload));
}

export function readCache(key, ttlMs = DEFAULT_TTL_MS) {
  const raw = localStorage.getItem(cacheKey(key));
  if (!raw) {
    return null;
  }
  try {
    const payload = JSON.parse(raw);
    if (!payload.ts || Date.now() - payload.ts > ttlMs) {
      return null;
    }
    return payload.value;
  } catch (error) {
    return null;
  }
}

export function invalidateCache(prefix = "") {
  Object.keys(localStorage).forEach((key) => {
    if (key.startsWith(cacheKey(prefix))) {
      localStorage.removeItem(key);
    }
  });
}

// Private helper functions for region data handling
function parseRegionPayload(payload) {
  if (!payload || payload.version !== REGION_DATA_VERSION) {
    return null;
  }
  return {
    regions: payload.regions || [],
    selectedRegion: payload.selectedRegion || '',
    regionZoneMap: payload.regionZoneMap || {},
    fromCache: true
  };
}

function loadFromLocalStorage() {
  try {
    const raw = localStorage.getItem(cacheKey(REGION_CACHE_KEY));
    if (raw) {
      const payload = JSON.parse(raw);
      if (Date.now() - payload.ts < REGION_CACHE_TTL_MS) {
        return parseRegionPayload(payload);
      }
    }
  } catch (e) {
    console.warn('localStorage read failed:', e);
  }
  return null;
}

function loadFromCookie() {
  try {
    const cookieName = cacheKey(REGION_CACHE_KEY).replace(PREFIX, 'cvm.');
    const cookie = document.cookie.split('; ').find(row => row.startsWith(`${cookieName}=`));
    if (cookie) {
      const value = decodeURIComponent(cookie.split('=')[1]);
      const payload = JSON.parse(value);
      if (Date.now() - payload.ts < REGION_CACHE_TTL_MS) {
        return parseRegionPayload(payload);
      }
    }
  } catch (e) {
    console.warn('Cookie read failed:', e);
  }
  return null;
}

function saveToCookie(payload) {
  try {
    const cookieValue = encodeURIComponent(JSON.stringify(payload));
    const expires = new Date(Date.now() + REGION_COOKIE_TTL_MS).toUTCString();
    const cookieName = cacheKey(REGION_CACHE_KEY).replace(PREFIX, 'cvm.');
    document.cookie = `${cookieName}=${cookieValue}; expires=${expires}; path=/`;
  } catch (e) {
    console.warn('Cookie save failed:', e);
  }
}

function clearCookie() {
  try {
    const cookieName = cacheKey(REGION_CACHE_KEY).replace(PREFIX, 'cvm.');
    document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
  } catch (e) {
    console.warn('Cookie clear failed:', e);
  }
}

export function saveRegionData(regions, selectedRegion, regionZoneMap) {
  const payload = {
    ts: Date.now(),
    regions,
    selectedRegion,
    regionZoneMap,
    version: REGION_DATA_VERSION
  };

  try {
    // 优先使用localStorage
    localStorage.setItem(cacheKey(REGION_CACHE_KEY), JSON.stringify(payload));
    console.log('Region data saved to localStorage');
  } catch (e) {
    console.warn('localStorage failed, falling back to cookie:', e);
    // Cookie备选方案
    saveToCookie(payload);
  }
}

export function loadRegionData() {
  // 尝试从localStorage加载
  const localStorageResult = loadFromLocalStorage();
  if (localStorageResult) {
    return localStorageResult;
  }

  // 尝试从cookie加载
  const cookieResult = loadFromCookie();
  if (cookieResult) {
    return cookieResult;
  }

  return {
    regions: [],
    selectedRegion: '',
    regionZoneMap: {},
    fromCache: false
  };
}

export function clearRegionData() {
  try {
    localStorage.removeItem(cacheKey(REGION_CACHE_KEY));
  } catch (e) {
    console.warn('localStorage remove failed:', e);
  }

  // 清除cookie
  clearCookie();
}
