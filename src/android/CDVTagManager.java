/**
 * Copyright (c) 2014 Jared Dickson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jareddickson.cordova.tagmanager;

import android.util.Log;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.DataLayer;
import com.google.android.gms.tagmanager.TagManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * This class echoes a string called from JavaScript.
 */
public class CDVTagManager extends CordovaPlugin {

    private boolean inited = false;
    private static GoogleAnalytics analytics;

    public CDVTagManager() {
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    private class ContainerLoadedCallback {

    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callback) {

        if (action.equals("initGTM")) {

            try {
                String containerId = args.getString(0);

                analytics = GoogleAnalytics.getInstance(cordova.getActivity());
                analytics.setLocalDispatchPeriod(args.getInt(1)); // Set the dispatch interval

                TagManager tagManager = TagManager.getInstance(cordova.getActivity());
                PendingResult pending = tagManager.loadContainerPreferNonDefault(containerId, -1);

                // The onResult method will be called as soon as one of the following happens:
                //     1. a saved container is loaded
                //     2. if there is no saved container, a network container is loaded
                //     3. the request times out. The example below uses a constant to manage the timeout period.
                pending.setResultCallback(new ResultCallback<ContainerHolder>() {
                    @Override
                    public void onResult(ContainerHolder containerHolder) {
                        ContainerHolderSingleton.setContainerHolder(containerHolder);
                        Container container = containerHolder.getContainer();
                        if (!containerHolder.getStatus().isSuccess()) {
                            Log.e("Error", "failure loading container");
                            callback.error("failure loading container");
                            return;
                        }
                        ContainerHolderSingleton.setContainerHolder(containerHolder);
//                        ContainerLoadedCallback.registerCallbacksForContainer(container);
//                        containerHolder.setContainerAvailableListener(new ContainerLoadedCallback());
//                        startMainActivity();
                        inited = true;
                    }
                }, 2, TimeUnit.SECONDS);

                callback.success("initGTM - id = " + args.getString(0) + "; interval = " + args.getInt(1) + " seconds");
                return true;
            } catch (final Exception e) {
                callback.error(e.getMessage());
            }
        } else if (action.equals("exitGTM")) {
            try {
                inited = false;
                callback.success("exitGTM");
                return true;
            } catch (final Exception e) {
                callback.error(e.getMessage());
            }
        } else if (action.equals("trackEvent")) {
            if (inited) {
                try {
                    DataLayer dataLayer = TagManager.getInstance(cordova.getActivity()).getDataLayer();
                    dataLayer.push(objectToMap(args.getJSONObject(0)));
                    callback.success("trackEvent");
                    return true;
                } catch (final Exception e) {
                    callback.error(e.getMessage());
                }
            } else {
                callback.error("trackEvent failed - not initialized");
            }
        } else if (action.equals("trackPage")) {
            if (inited) {
                try {
                    DataLayer dataLayer = TagManager.getInstance(cordova.getActivity()).getDataLayer();
                    dataLayer.push(objectToMap(args.getJSONObject(0)));
                    callback.success("trackPage");
                    return true;
                } catch (final Exception e) {
                    callback.error(e.getMessage());
                }
            } else {
                callback.error("trackPage failed - not initialized");
            }
        } else if (action.equals("trackException")) {
            if (inited) {
                try {
                    DataLayer dataLayer = TagManager.getInstance(cordova.getActivity()).getDataLayer();
                    dataLayer.push(objectToMap(args.getJSONObject(0)));
                    callback.success("trackException");
                    return true;
                } catch (final Exception e) {
                    callback.error(e.getMessage());
                }
            } else {
                callback.error("trackException failed - not initialized");
            }
        } else if (action.equals("dispatch")) {
            if (inited) {
                try {
                    GoogleAnalytics.getInstance(cordova.getActivity()).dispatchLocalHits();
                    callback.success("dispatch sent");
                    return true;
                } catch (final Exception e) {
                    callback.error(e.getMessage());
                }
            } else {
                callback.error("dispatch failed - not initialized");
            }
        }
        return false;
    }

    /**
     * Singleton to hold the GTM Container (since it should be only created once
     * per run of the app).
     */
    public static class ContainerHolderSingleton {
        private static ContainerHolder containerHolder;

        /**
         * Utility class; don't instantiate.
         */
        private ContainerHolderSingleton() {
        }

        public static ContainerHolder getContainerHolder() {
            return containerHolder;
        }

        public static void setContainerHolder(ContainerHolder c) {
            containerHolder = c;
        }
    }

    private static Map<String, Object> objectToMap(JSONObject o) throws JSONException {
        if (o.length() == 0) {
          return Collections.<String, Object>emptyMap();
        }
        Map<String, Object> map = new HashMap<String, Object>(o.length());
        Iterator it = o.keys();
        String key;
        Object value;
        while (it.hasNext()) {
          key = it.next().toString();
          value = o.has(key) ? o.get(key): null;
          map.put(key, value);
        }
        return map;
    }
}


// /**
//  * Copyright (c) 2014 Jared Dickson
//  *
//  * Permission is hereby granted, free of charge, to any person obtaining a copy
//  * of this software and associated documentation files (the "Software"), to deal
//  * in the Software without restriction, including without limitation the rights
//  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  * copies of the Software, and to permit persons to whom the Software is
//  * furnished to do so, subject to the following conditions:
//  *
//  * The above copyright notice and this permission notice shall be included in
//  * all copies or substantial portions of the Software.
//  *
//  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  * THE SOFTWARE.
//  */

// package com.jareddickson.cordova.tagmanager;

// import org.apache.cordova.CordovaWebView;
// import org.apache.cordova.CallbackContext;
// import org.apache.cordova.CordovaPlugin;
// import org.apache.cordova.CordovaInterface;

// import com.google.analytics.tracking.android.GAServiceManager;
// import com.google.android.gms.tagmanager.Container;
// import com.google.android.gms.tagmanager.ContainerOpener;
// import com.google.android.gms.tagmanager.ContainerOpener.OpenType;
// import com.google.android.gms.tagmanager.DataLayer;
// import com.google.android.gms.tagmanager.TagManager;

// import org.json.JSONArray;
// import org.json.JSONException;
// import org.json.JSONObject;

// import java.util.Collections;
// import java.util.Map;
// import java.util.HashMap;
// import java.util.Iterator;

// /**
//  * This class echoes a string called from JavaScript.
//  */
// public class CDVTagManager extends CordovaPlugin {

//     private Container mContainer;
//     private boolean inited = false;

//     public CDVTagManager() {
//     }

//     public void initialize(CordovaInterface cordova, CordovaWebView webView) {
//         super.initialize(cordova, webView);
//     }

//     @Override
//     public boolean execute(String action, JSONArray args, CallbackContext callback) {
//         if (action.equals("initGTM")) {
//             try {
//                 // Set the dispatch interval
//                 GAServiceManager.getInstance().setLocalDispatchPeriod(args.getInt(1));

//                 TagManager tagManager = TagManager.getInstance(this.cordova.getActivity().getApplicationContext());
//                 ContainerOpener.openContainer(
//                         tagManager,                             // TagManager instance.
//                         args.getString(0),                      // Tag Manager Container ID.
//                         OpenType.PREFER_NON_DEFAULT,            // Prefer not to get the default container, but stale is OK.
//                         null,                                   // Time to wait for saved container to load (ms). Default is 2000ms.
//                         new ContainerOpener.Notifier() {        // Called when container loads.
//                             @Override
//                             public void containerAvailable(Container container) {
//                                 // Handle assignment in callback to avoid blocking main thread.
//                                 mContainer = container;
//                                 inited = true;
//                             }
//                         }
//                 );
//                 callback.success("initGTM - id = " + args.getString(0) + "; interval = " + args.getInt(1) + " seconds");
//                 return true;
//             } catch (final Exception e) {
//                 callback.error(e.getMessage());
//             }
//         } else if (action.equals("exitGTM")) {
//             try {
//                 inited = false;
//                 callback.success("exitGTM");
//                 return true;
//             } catch (final Exception e) {
//                 callback.error(e.getMessage());
//             }
//         } else if (action.equals("trackEvent")) {
//             if (inited) {
//                 try {
//                     DataLayer dataLayer = TagManager.getInstance(this.cordova.getActivity().getApplicationContext()).getDataLayer();
//                     dataLayer.push(objectToMap(args.getJSONObject(0)));
//                     callback.success("trackEvent");
//                     return true;
//                 } catch (final Exception e) {
//                     callback.error(e.getMessage());
//                 }
//             } else {
//                 callback.error("trackEvent failed - not initialized");
//             }
//         } else if (action.equals("trackPage")) {
//             if (inited) {
//                 try {
//                     DataLayer dataLayer = TagManager.getInstance(this.cordova.getActivity().getApplicationContext()).getDataLayer();
//                     dataLayer.push(objectToMap(args.getJSONObject(0)));
//                     callback.success("trackPage");
//                     return true;
//                 } catch (final Exception e) {
//                     callback.error(e.getMessage());
//                 }
//             } else {
//                 callback.error("trackPage failed - not initialized");
//             }
//         } else if (action.equals("trackException")) {
//             if (inited) {
//                 try {
//                     DataLayer dataLayer = TagManager.getInstance(this.cordova.getActivity().getApplicationContext()).getDataLayer();
//                     dataLayer.push(objectToMap(args.getJSONObject(0)));
//                     callback.success("trackException");
//                     return true;
//                 } catch (final Exception e) {
//                     callback.error(e.getMessage());
//                 }
//             } else {
//                 callback.error("trackException failed - not initialized");
//             }
//         } else if (action.equals("dispatch")) {
//             if (inited) {
//                 try {
//                     GAServiceManager.getInstance().dispatchLocalHits();
//                     callback.success("dispatch sent");
//                     return true;
//                 } catch (final Exception e) {
//                     callback.error(e.getMessage());
//                 }
//             } else {
//                 callback.error("dispatch failed - not initialized");
//             }
//         }
//         return false;
//     }

//     private static Map<String, Object> objectToMap(JSONObject o) throws JSONException {
//         if (o.length() == 0) {
//           return Collections.<String, Object>emptyMap();
//         }
//         Map<String, Object> map = new HashMap<String, Object>(o.length());
//         Iterator it = o.keys();
//         String key;
//         Object value;
//         while (it.hasNext()) {
//           key = it.next().toString();
//           value = o.has(key) ? o.get(key): null;
//           map.put(key, value);
//         }
//         return map;
//     }
// }
