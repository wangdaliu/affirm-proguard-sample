package com.affirm.proguard.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.affirm.android.Affirm
import com.affirm.android.Affirm.configureWithAmount
import com.affirm.android.AffirmPromotionButton
import com.affirm.android.model.Address
import com.affirm.android.model.Billing
import com.affirm.android.model.Checkout
import com.affirm.android.model.Currency
import com.affirm.android.model.Item
import com.affirm.android.model.Name
import com.affirm.android.model.PromoPageType
import com.affirm.android.model.Shipping
import com.affirm.proguard.sample.ui.theme.AffirmproguardsampleTheme
import java.math.BigDecimal

class MainActivity : ComponentActivity(), Affirm.CheckoutCallbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AffirmproguardsampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Color.White) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        AffirmPromotionButton(modifier = Modifier.padding(8.dp))

                        CheckoutButton(modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Affirm.handleCheckoutData(this, requestCode, resultCode, data)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onAffirmCheckoutError(message: String?) {
        Toast.makeText(this, "Checkout Error: $message", Toast.LENGTH_LONG).show()
    }

    override fun onAffirmCheckoutCancelled() {
        Toast.makeText(this, "Checkout Cancelled", Toast.LENGTH_LONG).show()
    }

    override fun onAffirmCheckoutSuccess(token: String) {
        Toast.makeText(this, "Checkout token: $token", Toast.LENGTH_LONG).show()
    }
}

private fun checkoutModel(): Checkout {
    val item = Item.builder()
        .setDisplayName("Great Deal Wheel")
        .setImageUrl(
            "http://www.m2motorsportinc.com/media/catalog/product/cache/1/thumbnail" +
                "/9df78eab33525d08d6e5fb8d27136e95/v/e/velocity-vw125-wheels-rims.jpg")
        .setQty(1)
        .setSku("wheel")
        .setUnitPrice(BigDecimal.valueOf(1000.0))
        .setUrl("http://merchant.com/great_deal_wheel")
        .setCategories(listOf(listOf("Apparel", "Pants"), listOf("Mens", "Apparel", "Pants")))
        .build()
    val items: MutableMap<String, Item> = HashMap()
    items["wheel"] = item
    val name = Name.builder().setFull("John Smith").build()

    val address = Address.builder()
        .setCity("San Francisco")
        .setCountry("USA")
        .setStreet1("333 Kansas st")
        .setRegion1Code("CA")
        .setPostalCode("94107")
        .build()

    val shipping = Shipping.builder().setAddress(address).setName(name).build()
    val billing = Billing.builder().setAddress(address).setName(name).build()

    // More details on https://docs.affirm.com/affirm-developers/reference/the-metadata-object
    val metadata = mapOf(
        "webhook_session_id" to "ABC123",
        "shipping_type" to "UPS Ground",
        "entity_name" to "internal-sub_brand-name"
    )

    return Checkout.builder()
        .setItems(items)
        .setBilling(billing)
        .setShipping(shipping)
        .setShippingAmount(BigDecimal.valueOf(0.0))
        .setTaxAmount(BigDecimal.valueOf(100.0))
        .setTotal(BigDecimal.valueOf(1100.0))
        .setCurrency(Currency.USD)
        .setMetadata(metadata)
        .build()
}

@Composable
fun CheckoutButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Button(
        onClick = {
            try {
                val activity = context as? Activity
                if (activity != null) {
                    Affirm.startCheckout(activity, checkoutModel(), null, 10, false)
                } else {
                    Toast.makeText(context, "Context is not an Activity", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Checkout failed, reason: $e", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = modifier
    ) {
        Text("Checkout")
    }
}

@Composable
fun AffirmPromotionButton(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            val view =
                LayoutInflater.from(context).inflate(R.layout.affirm_promotion_button, null, false)

            val affirmPromotionButton = view.findViewById<AffirmPromotionButton>(R.id.promo).apply {
                configureWithAmount(
                    this,
                    null,
                    PromoPageType.PRODUCT, BigDecimal.valueOf(1100.0), true
                )
            }
            affirmPromotionButton
        },
        modifier = modifier
    )
}