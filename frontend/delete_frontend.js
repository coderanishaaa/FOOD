const fs = require('fs');
['src/components/StripeCheckout.jsx', 'src/pages/PaymentSuccess.jsx'].forEach(f => {
    if (fs.existsSync(f)) fs.unlinkSync(f);
});
