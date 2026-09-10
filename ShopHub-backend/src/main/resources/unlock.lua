-- KEYS[1]: lock key, ARGV[1]: lock owner token
-- check if lock key' value is equal to thread id
if (redis.call('GET', KEYS[1]) == ARGV[1]) then
  -- equal, delete lock
  return redis.call('DEL', KEYS[1])
end
-- not equal, do nothing
return 0
