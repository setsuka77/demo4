// クリックイベントを一度だけ設定する関数
function setUpClickListener(selector, callback) {
	document.querySelectorAll(selector).forEach(item => {
		item.addEventListener('click', callback);
	});
}

// 削除リンクをクリックしたときにダイアログを表示
setUpClickListener('.delete-link', function(event) {
	event.preventDefault(); // クリック時のデフォルト動作を防ぐ

	const productId = this.getAttribute('data-product-id');
	const dialog = document.getElementById('confirmDialog');

	// ダイアログを表示
	dialog.style.display = 'block';

	// 削除ボタンがクリックされたら、削除処理を実行
	document.getElementById('deleteBtn').onclick = () => handleProductDelete(productId);

	// キャンセルボタンがクリックされたら、ダイアログを閉じる
	document.getElementById('cancel').onclick = () => dialog.style.display = 'none';
});

// 商品削除処理
function handleProductDelete(productId) {
	const form = document.getElementById('cart');
	const input = document.createElement('input');
	input.type = 'hidden';
	input.name = 'productId';  // パラメータ名を指定
	input.value = productId;  // 削除するカートのID
	form.appendChild(input);
	form.submit();  // フォームを送信
}

// ステッパーのイベント処理
setUpClickListener('.stepper .increment', function(event) {
	event.preventDefault(); // デフォルト動作を無効化
	const stepper = this.closest('.stepper');
	const input = stepper.querySelector('.stepper-input');
	handleQuantityChange(input, 1);
});

setUpClickListener('.stepper .decrement', function(event) {
	event.preventDefault(); // デフォルト動作を無効化
	const stepper = this.closest('.stepper');
	const input = stepper.querySelector('.stepper-input');
	handleQuantityChange(input, -1);
});

// 数量変更処理
function handleQuantityChange(input, step) {
	const max = parseInt(input.getAttribute('max')) || Infinity;
	const min = parseInt(input.getAttribute('min')) || 0;
	const current = parseInt(input.value) || 0;
	const newValue = current + step;

	if (newValue >= min && newValue <= max) {
		input.value = newValue;
		const productId = input.closest('.cart-item').querySelector('.delete-link').getAttribute('data-product-id');
		fetchUpdateQuantity(productId, newValue);
	}
}

// 数量更新処理
function fetchUpdateQuantity(productId, newQuantity) {
	fetch('/demo/cart/update', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({ productId, quantity: newQuantity })
	})
		.then(response => response.json())
		.then(data => {
			const { productId, quantity } = data.cartDto;
			updateSubtotal(productId, quantity);
			updateTotalItems();
		})
		.catch(error => console.error('エラーが発生しました', error));
}

// 小計の更新
function updateSubtotal(productId, quantity) {
	const cartItem = document.querySelector(`.cart-item[data-product-id="${productId}"]`);
	if (!cartItem) return;

	const unitPrice = parseFloat(cartItem.querySelector('.price').getAttribute('data-original-price')) || 0;
	const subtotal = quantity * unitPrice;
	const subtotalElement = cartItem.querySelector('.subtotal-price');

	subtotalElement.textContent = formatCurrency(subtotal);
	updateProductAmount();
}

// 商品金額の更新
function updateProductAmount() {
	let total = 0;
	document.querySelectorAll('.subtotal-price').forEach(subtotalElement => {
		const subtotal = parseFloat(subtotalElement.textContent.replace(/[^\d.-]/g, '')) || 0;
		total += subtotal;
	});
	document.getElementById('totalAmount').textContent = formatCurrency(total);
}

// totalItemsを更新する関数
function updateTotalItems() {
	let totalItems = 0;
	document.querySelectorAll('.stepper-input').forEach(input => {
		totalItems += parseInt(input.value) || 0;
	});
	document.getElementById('totalNumber').textContent = `${totalItems}件`;
}

// 価格をフォーマットする関数
function formatCurrency(amount) {
	return new Intl.NumberFormat('ja-JP', {
		style: 'currency',
		currency: 'JPY'
	}).format(amount);
}

// ページのロード時に価格をフォーマットする
window.onload = function() {
	formatPrices();
	updateProductAmount();
	initializeTotalWithDiscount(); // 合計を初期化
};

// 価格の初期フォーマット
function formatPrices() {
	document.querySelectorAll('.price, .subtotal-price').forEach(priceElement => {
		const originalPrice = priceElement.getAttribute('data-original-price') || priceElement.textContent.trim();
		priceElement.textContent = formatCurrency(originalPrice);
	});
}

// 合計を初期化する関数
function initializeTotalWithDiscount() {
	const totalAmountElement = document.getElementById('totalAmount');
	const totalWithDiscountElement = document.getElementById('totalWithDiscount');

	// 商品合計の値を取得して合計に反映
	const totalAmount = parseFloat(totalAmountElement.textContent.replace(/[^\d.-]/g, '')) || 0;
	totalWithDiscountElement.textContent = `￥${totalAmount.toLocaleString('ja-JP', { minimumFractionDigits: 0 })}`;
}

// クーポンコード入力処理
document.querySelector('button[name="action"][value="applyCoupon"]').addEventListener('click', () => {
	const couponCode = document.getElementById('couponCodeInput').value;
	if (couponCode) applyCoupon(couponCode);
});

// クーポン適用処理
function applyCoupon(couponCode) {
	fetch('/demo/cart/coupon', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ couponCode })
	})
		.then(response => response.json())
		.then(data => {
			if (data.message) {
				displayErrorMessage(data.message);
			} else {
				updateCouponInfo(data.coupon.discountAmount);
			}
		})
		.catch(error => console.error('クーポン適用中にエラーが発生しました:', error));
}

// クーポン無効時の処理
function displayErrorMessage(message) {
	const discountContainer = document.querySelector('.discount-info');
	discountContainer.textContent = message;
	discountContainer.classList.add('discountMessage');
}

// クーポン適用後の情報更新
function updateCouponInfo(discountAmount) {
	const totalAmountElement = document.getElementById('totalAmount');
	const discountRow = document.getElementById('discountRow');
	const discountTextElement = document.getElementById('discountText');
	const totalWithDiscountElement = document.getElementById('totalWithDiscount');

	const totalAmount = parseFloat(totalAmountElement.textContent.replace(/[^\d.-]/g, '')) || 0;
	const discountValue = discountAmount < 1 ? totalAmount * discountAmount : discountAmount;
	const discountText = discountAmount < 1
		? `${(discountAmount * 100).toFixed(0)}%オフ -￥${discountValue.toLocaleString('ja-JP', { minimumFractionDigits: 0 })}`
		: `￥${discountValue.toLocaleString('ja-JP', { minimumFractionDigits: 0 })}オフ`;

	discountRow.style.display = 'flex';
	discountTextElement.textContent = discountText;

	const totalWithDiscount = totalAmount - discountValue;
	totalWithDiscountElement.textContent = `￥${totalWithDiscount.toLocaleString('ja-JP', { minimumFractionDigits: 0 })}`;

	// ボタンのテキストを「変更」にする
	const applyCouponButton = document.querySelector('button[name="action"][value="applyCoupon"]');
	applyCouponButton.textContent = '変更';

	// 「削除」リンクのクリックイベントを設定
	document.getElementById('removeDiscount').addEventListener('click', (event) => {
		event.preventDefault();
		resetCoupon();
	});
}

// クーポンをリセットする関数
function resetCoupon() {
	const discountRow = document.getElementById('discountRow');
	const discountTextElement = document.getElementById('discountText');
	const totalWithDiscountElement = document.getElementById('totalWithDiscount');
	const totalAmountElement = document.getElementById('totalAmount');

	// 割引情報を非表示にし、初期状態に戻す
	discountRow.style.display = 'none';
	discountTextElement.textContent = '';
	totalWithDiscountElement.textContent = totalAmountElement.textContent;

	// 入力フォームをクリア
	document.getElementById('couponCodeInput').value = '';
}