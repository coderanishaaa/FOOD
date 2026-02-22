const fs = require('fs');
const files = [
    'src/main/java/com/fooddelivery/payment/service/MockPaymentService.java',
    'src/main/java/com/fooddelivery/payment/service/StripeService.java',
    'src/main/java/com/fooddelivery/payment/dto/CheckoutSessionResponse.java',
    'src/main/java/com/fooddelivery/payment/config/StripeConfig.java'
];

files.forEach(file => {
    if (fs.existsSync(file)) {
        fs.unlinkSync(file);
        console.log(`Deleted ${file}`);
    }
});
