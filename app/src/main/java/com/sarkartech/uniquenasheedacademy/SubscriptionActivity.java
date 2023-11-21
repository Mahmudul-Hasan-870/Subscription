package com.sarkartech.uniquenasheedacademy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList;
import com.sarkartech.uniquenasheedacademy.databinding.ActivitySubscriptionBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubscriptionActivity extends AppCompatActivity {

    ActivitySubscriptionBinding binding;

    RecyclerView recyclerView;
    ArrayList<itemDS> itemLST;
    private BillingClient billingClient;

    boolean inSuccess = false;
    String productId, dur;
    int planeIdx;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        productId = "monthly_subscription";
        context = SubscriptionActivity.this;

        billingClient = BillingClient.newBuilder(context).setListener(purchasesUpdatedListener).enablePendingPurchases().build();


        itemLST = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (inSuccess) {
            binding.tvSubstatus.setText("Subscription Status: Already Subscribed");
        } else {
            binding.tvSubstatus.setText("Subscription Status: Not Subscribed");
        }
        show_list();
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> Purchase) {
            // To be implemented in a later section.
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && Purchase != null) {
                for (Purchase purchase : Purchase){
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                binding.tvSubstatus.setText("Subscription Status: Already Subscribed");
                inSuccess = true;
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
                binding.tvSubstatus.setText("Subscription Status: Feature Not Supported");
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                // Billing service is unavailable on this device
                binding.tvSubstatus.setText("Subscription Status: Billing Unavailable");
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // User canceled the subscription process
                binding.tvSubstatus.setText("Subscription Status: Subscription Canceled");
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
                // Developer error occurred
                binding.tvSubstatus.setText("Subscription Status: Developer Error");
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                // The requested item is unavailable
                binding.tvSubstatus.setText("Subscription Status: Item Unavailable");
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR) {
                // Network error occurred
                binding.tvSubstatus.setText("Subscription Status: Network Error");
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                // Billing service is disconnected
                binding.tvSubstatus.setText("Subscription Status: Service Disconnected");
            } else {
                Toast.makeText(context, "Error: " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void show_list() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(BillingClient.ProductType.SUBS).build())).build();
                    billingClient.queryProductDetailsAsync(queryProductDetailsParams, (billingResult1, productDetailsList) -> {
                        for (ProductDetails productDetails : productDetailsList) {
                            for (int i = 0; i <= (productDetails.getSubscriptionOfferDetails().size()); i++) {
                                String subName = null;
                                String status = null;
                                if (i == 0) {
                                    subName = productDetails.getName();
                                }
                                int index = i;
                                String phases;
                                String formattedPrice = productDetails.getSubscriptionOfferDetails().get(i).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                                String billingPeriod = productDetails.getSubscriptionOfferDetails().get(i).getPricingPhases().getPricingPhaseList().get(0).getBillingPeriod();
                                int recurrenceMode = productDetails.getSubscriptionOfferDetails().get(i).getPricingPhases().getPricingPhaseList().get(0).getRecurrenceMode();
                                String n, duration, bp;
                                bp = billingPeriod;
                                n = billingPeriod.substring(1, 2);
                                duration = billingPeriod.substring(2, 3);
                                int nPanses = productDetails.getSubscriptionOfferDetails().get(i).getPricingPhases().getPricingPhaseList().size();
                                if (recurrenceMode == 2) {
                                    if (duration.equals("M")) {
                                        dur = " For " + n + " Month";
                                    } else if (duration.equals("Y")) {
                                        dur = " For " + n + " Year";

                                    } else if (duration.equals("W")) {
                                        dur = " For " + n + " Week";
                                    } else if (duration.equals("D")) {
                                        dur = " For " + n + " Days";
                                    }
                                } else {
                                    if (bp.equals("P1M")) {
                                        dur = "/Monthly";
                                    } else if (bp.equals("P6M")) {
                                        dur = "/Every 6 Month";
                                    } else if (bp.equals("P1Y")) {
                                        dur = "/Yearly";
                                    } else if (bp.equals("P1W")) {
                                        dur = "/Weekly";
                                    } else if (bp.equals("P3W")) {
                                        dur = "/Every 3 Week";
                                    }
                                }
                                phases = formattedPrice + "" + dur;

                                //
                                for (int j = 0; j <= nPanses - 1; j++) {
                                    if (j > 0) {

                                        String price = productDetails.getSubscriptionOfferDetails().get(i).getPricingPhases().getPricingPhaseList().get(j).getFormattedPrice();

                                        String period = productDetails.getSubscriptionOfferDetails().get(i).getPricingPhases().getPricingPhaseList().get(j).getBillingPeriod();

                                        if (period.equals("P1M")) {
                                            dur = "/Monthly";
                                        } else if (period.equals("P6M")) {
                                            dur = "/Every 6 Month";
                                        } else if (period.equals("P1Y")) {
                                            dur = "/Yearly";
                                        } else if (period.equals("P1W")) {
                                            dur = "/Weekly";
                                        } else if (period.equals("P3W")) {
                                            dur = "/Every 3 Week";
                                        } else {
                                            dur = "";
                                        }
                                        phases += "\n" + price + dur;

/*
                                        subName = productDetails.getSubscriptionOfferDetails().get(i).getOfferId();
*/
                                    }
                                }
                                itemLST.add(new itemDS("Premium Subscription", phases, subName, index));
                            }
                        }

                    });
                    runOnUiThread(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();


                        }
                        Adapter adapter = new Adapter(context, itemLST);
                        recyclerView.setAdapter(adapter);
                        adapter.setOnItemClickListener((parent, view, position, id) -> {
                           /* String p = String.valueOf(position);
                            Toast.makeText(getApplicationContext(), p, Toast.LENGTH_SHORT).show();*/
                            itemDS cItem = itemLST.get(position);
                            planeIdx = cItem.planeIndex;
                            subscribeProduct();
                        });
                    });
                });
            }
        });
    }

    private void subscribeProduct() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(BillingClient.ProductType.SUBS).build())).build();
                billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
                    @Override
                    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> productDetailsList) {
                        for (ProductDetails productDetails : productDetailsList) {
                            String offerToken = productDetails.getSubscriptionOfferDetails().get(planeIdx).getOfferToken();
                            ImmutableList productDetailsParamsList = ImmutableList.of(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).setOfferToken(offerToken).build());
                            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build();
                            billingClient.launchBillingFlow(SubscriptionActivity.this, billingFlowParams);
                        }
                    }
                });
            }

        });
    }

    public class itemDS {
        public itemDS(String planeName, String planePrice, String planeStatus, int planeIndex) {
            this.planeName = planeName;
            this.planePrice = planePrice;
            this.planeStatus = planeStatus;
            this.planeIndex = planeIndex;
        }

        String planeName, planePrice, planeStatus;
        int planeIndex;
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        Context context;
        ArrayList<itemDS> itemLST;
        private AdapterView.OnItemClickListener mListener;

        public Adapter(Context context, ArrayList<itemDS> itemLST) {
            this.context = context;
            this.itemLST = itemLST;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
            mListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.subscription_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView, mListener);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.name.setText(itemLST.get(position).planeName);
            holder.price.setText(itemLST.get(position).planePrice);
        }

        @Override
        public int getItemCount() {
            return itemLST.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, price;

            public ViewHolder(@NonNull View itemView, final AdapterView.OnItemClickListener listener) {
                super(itemView);
                name = itemView.findViewById(R.id.tvSubPlan);
                price = itemView.findViewById(R.id.tvSubPrice);

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(null, v, position, 0);
                        }
                    }
                });
            }
        }
    }

    private boolean verifyValidSignature(String signeData, String signture) {
        try {
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjBTfu5KEZsLC0inE+uBTQcDlKGduKNBtWAOJasduBYvrNUd5PC3WsjIXyMS9xlJVBJNT1c5G8MHsct1fzTdjVFIL8/JksRwL0dbsOnimC7PJmowz6vs5h8wSSl7pGIM/9ccQSA5sWmyaiVRZEgwymGKfh6wiHrFbBJlxYlEg7pl+iGSszTANBCmDk3ANyOyat8VDl96DF0jes/D2bUAraKrN2PKItSnGKRBvTtlSEhDH5Zu5ti5ibmoUs0KFsDOjrMOzhc1x0IfDIEY5cKAtSdOg2Upag/am6bPogiUWLB4h4c8/ZJRApMzudMUjMNqL5FPIue6czzVF+Yqk9+zdwQIDAQAB";
            return Security.verifyPurchase(base64Key, signeData, signture);
        } catch (IOException e) {
            return false;
        }
    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            inSuccess = true;
        }
    };

    void handlePurchase(final Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken()
                ).build();
        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                }
            }
        };
        billingClient.consumeAsync(consumeParams, listener);
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                Toast.makeText(context, "Error : invalid Purchase", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                inSuccess = true;
            } else {
                Toast.makeText(context, "Already Subscribed", Toast.LENGTH_SHORT).show();
            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            Toast.makeText(context, "Subscription Pending", Toast.LENGTH_SHORT).show();
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            Toast.makeText(context, "UNSPECIFIED_STATE", Toast.LENGTH_SHORT).show();
        }
    }
}