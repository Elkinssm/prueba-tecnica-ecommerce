-- Create order status history table to track all status changes
CREATE TABLE order_status_history (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Create index for efficient queries by order_id
CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);

-- Create index for queries by changed_at
CREATE INDEX idx_order_status_history_changed_at ON order_status_history(changed_at);
