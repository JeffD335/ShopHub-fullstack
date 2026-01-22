-- KEYS[1]: key of lock，ARGV[1]: current thread id
-- check if lock key' value is equal to thread id
if (redis.call('GET', KEYS[1]) == ARGV[1]) then
  -- equal, delete lock
  return redis.call('DEL', KEYS[1])
end
-- not equal, do nothing
return 0