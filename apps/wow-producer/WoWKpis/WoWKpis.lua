-- WowKpis.lua
-- Envia líneas con prefijo KAFKA_EXPORT:: como WHISPER a ti mismo.
-- Activa /chatlog en juego para que se escriba en Logs/WoWChatLog.txt

WowKpisDB = WowKpisDB or {}

local f = CreateFrame("Frame")
f:RegisterEvent("PLAYER_ENTERING_WORLD")
f:RegisterEvent("PLAYER_XP_UPDATE")
f:RegisterEvent("PLAYER_MONEY")
f:RegisterEvent("CHAT_MSG_COMBAT_XP_GAIN")
f:RegisterEvent("QUEST_TURNED_IN")

local lastSentAt = 0
local minInterval = 1.0  -- throttle (segundos)
local lastXP, lastMoney = 0, 0

local function sendJson(tbl)
  local who = UnitName("player")
  if not who then return end
  local now = GetTime()
  if now - lastSentAt < minInterval then return end
  lastSentAt = now

  -- JSON plano y prefijo estable (tu producer busca esto)
  local json = string.format(
    "KAFKA_EXPORT::{\"character\":\"%s-%s\",\"ts\":%d,\"xp\":%d,\"xp_max\":%d,\"copper\":%d}",
    UnitName("player") or "Unknown",
    GetRealmName() or "Unknown",
    time(),
    UnitXP("player") or 0,
    UnitXPMax("player") or 0,
    GetMoney() or 0
  )

  -- WHISPER a ti mismo → se guarda en WoWChatLog si /chatlog está activo
  SendChatMessage(json, "WHISPER", nil, who)
end

local function onEvent(self, event, ...)
  if event == "PLAYER_ENTERING_WORLD" then
    -- instant snapshot
    sendJson({})
    lastXP = UnitXP("player") or 0
    lastMoney = GetMoney() or 0

  elseif event == "PLAYER_XP_UPDATE" or event == "CHAT_MSG_COMBAT_XP_GAIN" or event == "QUEST_TURNED_IN" then
    local xp = UnitXP("player") or 0
    if xp ~= lastXP then
      lastXP = xp
      sendJson({})
    end

  elseif event == "PLAYER_MONEY" then
    local copper = GetMoney() or 0
    if copper ~= lastMoney then
      lastMoney = copper
      sendJson({})
    end
  end
end

f:SetScript("OnEvent", onEvent)
