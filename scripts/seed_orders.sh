#!/bin/bash

API_URL="http://localhost:8080/orders"

# Array of customers
customers=(
  '{"name":"Rajesh Kumar","email":"rajesh.kumar@gmail.com","phone":"+919876543210","address":{"street":"123 MG Road","city":"Mumbai","state":"Maharashtra","postalCode":"400001","country":"India"}}'
  '{"name":"Priya Sharma","email":"priya.sharma@yahoo.com","phone":"+919876543211","address":{"street":"45 Park Street","city":"Kolkata","state":"West Bengal","postalCode":"700016","country":"India"}}'
  '{"name":"Amit Patel","email":"amit.patel@outlook.com","phone":"+919876543212","address":{"street":"78 Brigade Road","city":"Bangalore","state":"Karnataka","postalCode":"560001","country":"India"}}'
  '{"name":"Sneha Reddy","email":"sneha.reddy@gmail.com","phone":"+919876543213","address":{"street":"12 Jubilee Hills","city":"Hyderabad","state":"Telangana","postalCode":"500033","country":"India"}}'
  '{"name":"Vikas Singh","email":"vikas.singh@gmail.com","phone":"+919876543214","address":{"street":"89 Civil Lines","city":"Delhi","state":"Delhi","postalCode":"110054","country":"India"}}'
  '{"name":"Anjali Verma","email":"anjali.verma@hotmail.com","phone":"+919876543215","address":{"street":"34 Anna Salai","city":"Chennai","state":"Tamil Nadu","postalCode":"600002","country":"India"}}'
  '{"name":"Rahul Gupta","email":"rahul.gupta@gmail.com","phone":"+919876543216","address":{"street":"56 FC Road","city":"Pune","state":"Maharashtra","postalCode":"411004","country":"India"}}'
  '{"name":"Neha Joshi","email":"neha.joshi@gmail.com","phone":"+919876543217","address":{"street":"23 CG Road","city":"Ahmedabad","state":"Gujarat","postalCode":"380009","country":"India"}}'
)

# Array of products
products=(
  '{"name":"Wireless Headphones","price":2499.00}'
  '{"name":"Smart Watch","price":8999.00}'
  '{"name":"Laptop Bag","price":1299.00}'
  '{"name":"USB-C Cable","price":399.00}'
  '{"name":"Phone Case","price":599.00}'
  '{"name":"Power Bank","price":1899.00}'
  '{"name":"Bluetooth Speaker","price":3499.00}'
  '{"name":"Keyboard","price":2199.00}'
  '{"name":"Mouse","price":899.00}'
  '{"name":"Webcam","price":4599.00}'
  '{"name":"Monitor Stand","price":1599.00}'
  '{"name":"HDMI Cable","price":499.00}'
  '{"name":"Screen Protector","price":299.00}'
  '{"name":"Charging Dock","price":1999.00}'
  '{"name":"Earbuds","price":3999.00}'
)

# Array of merchants
merchants=("MERCHANT001" "MERCHANT002" "MERCHANT003" "MERCHANT004" "MERCHANT005")

# Array of channels
channels=("WEB" "MOBILE" "API" "POS")

# Array of order types
order_types=("ONLINE" "OFFLINE")

# Function to generate random order
generate_order() {
  local order_num=$1
  local customer_index=$((RANDOM % ${#customers[@]}))
  local merchant_index=$((RANDOM % ${#merchants[@]}))
  local channel_index=$((RANDOM % ${#channels[@]}))
  local order_type_index=$((RANDOM % ${#order_types[@]}))
  
  local customer=${customers[$customer_index]}
  local merchant=${merchants[$merchant_index]}
  local channel=${channels[$channel_index]}
  local order_type=${order_types[$order_type_index]}
  
  # Generate 1-4 items per order
  local num_items=$((RANDOM % 4 + 1))
  local items=""
  local total_amount=0
  
  for ((i=0; i<num_items; i++)); do
    local product_index=$((RANDOM % ${#products[@]}))
    local product_line=${products[$product_index]}
    local product_name=$(echo "$product_line" | sed 's/.*"name":"\([^"]*\)".*/\1/')
    local product_price=$(echo "$product_line" | sed 's/.*"price":\([0-9.]*\).*/\1/')
    local quantity=$((RANDOM % 3 + 1))
    local item_total=$(echo "$product_price * $quantity" | bc)
    total_amount=$(echo "$total_amount + $item_total" | bc)
    
    if [ $i -gt 0 ]; then
      items="$items,"
    fi
    items="$items{\"name\":\"$product_name\",\"quantity\":$quantity,\"unitPrice\":$product_price}"
  done
  
  local order_id="ORD$(date +%s)$order_num"
  local merchant_ref="REF$(date +%s)$order_num"
  
  local payload=$(cat <<EOF
{
  "orderId": "$order_id",
  "orderType": "$order_type",
  "merchantOrderReference": "$merchant_ref",
  "orderAmount": {
    "value": $total_amount,
    "currency": "INR"
  },
  "merchantId": "$merchant",
  "channel": "$channel",
  "customer": $customer,
  "orderDetails": {
    "items": [$items],
    "metadata": {
      "note": "Order placed via $channel",
      "source": "$(echo $channel | tr '[:upper:]' '[:lower:]')"
    }
  }
}
EOF
)

  echo "Creating order $order_num: $order_id"
  curl -s -X POST "$API_URL" \
    -H "Content-Type: application/json" \
    -d "$payload"
  
  echo ""
  sleep 0.5
}

echo "Starting to create orders..."
echo "================================"

# Create 200 orders
for i in {1..200}; do
  generate_order $i
done

echo ""
echo "================================"
echo "Completed creating 200 orders"
