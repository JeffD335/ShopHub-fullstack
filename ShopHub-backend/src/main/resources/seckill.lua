--1. list of arguments
    --1.1 voucher id
    local voucherId = ARGV[1];
    --1.2 user id
    local userId = ARGV[2]
    -- 1.3.order id
    local orderId = ARGV[3]
--2. key of data
    --2.1 voucher stock key
    local stockKey = 'seckill:stock:'..voucherId
    --2.2 voucherOrder key
    local orderKey = 'seckill:order:'..voucherId
--3. script
-- stock is insufficient, return 1
if(tonumber(redis.call('get', stockKey)) < 0) then
    return 1
end
-- userId is in the set of order, return 2
if(redis.call('sismember', orderKey, userId) == 1) then
    return 2
end
redis.call('incrby', stockKey, -1)
redis.call('sadd', orderKey, userId)

-- send to MQ
redis.call('XADD', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)

return 0
