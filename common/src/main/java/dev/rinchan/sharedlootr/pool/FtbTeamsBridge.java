package dev.rinchan.sharedlootr.pool;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * Zero-dependency reflective bridge to FTB Teams (if installed).
 * Returns {@code null} when FTB Teams is absent or the player is not in a shared party/server team.
 */
public final class FtbTeamsBridge {
    private static final String FTB_TEAMS_API_CLASS = "dev.ftb.mods.ftbteams.api.FTBTeamsAPI";

    private static volatile boolean probed;
    private static volatile Method apiMethod;

    private FtbTeamsBridge() {
    }

    public static UUID resolveServerTeam(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }
        Object api = getApi();
        if (api == null) {
            return null;
        }
        try {
            if (!invokeBoolean(api, "isManagerLoaded")) {
                return null;
            }
            Object manager = invoke(api, "getManager");
            if (manager == null) {
                return null;
            }
            Object teamOpt = invoke(manager, "getTeamForPlayerID", new Class<?>[]{UUID.class}, new Object[]{playerUuid});
            Object team = unwrapOptional(teamOpt);
            return extractSharedTeamUuid(team);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static UUID resolveClientTeam(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }
        Object api = getApi();
        if (api == null) {
            return null;
        }
        try {
            if (!invokeBoolean(api, "isClientManagerLoaded")) {
                return null;
            }
            Object clientManager = invoke(api, "getClientManager");
            if (clientManager == null) {
                return null;
            }
            Object knownOpt = invoke(clientManager, "getKnownPlayer", new Class<?>[]{UUID.class}, new Object[]{playerUuid});
            Object known = unwrapOptional(knownOpt);
            if (known != null) {
                Object teamId = invoke(known, "teamId");
                if (teamId instanceof UUID uuid) {
                    Object teamOpt = invoke(clientManager, "getTeamByID", new Class<?>[]{UUID.class}, new Object[]{uuid});
                    Object team = unwrapOptional(teamOpt);
                    return extractSharedTeamUuid(team);
                }
            }
            Object selfTeam = invoke(clientManager, "selfTeam");
            return extractSharedTeamUuid(selfTeam);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static UUID extractSharedTeamUuid(Object team) throws Exception {
        if (team == null) {
            return null;
        }
        // Solo FTB personal teams are treated as non-party so soloPlayersSharePool and vanilla teams still apply.
        if (invokeOptionalBoolean(team, "isPlayerTeam")) {
            return null;
        }
        Object id = invoke(team, "getId");
        return id instanceof UUID uuid ? uuid : null;
    }

    private static Object getApi() {
        if (!probed) {
            synchronized (FtbTeamsBridge.class) {
                if (!probed) {
                    try {
                        Class<?> apiClass = Class.forName(FTB_TEAMS_API_CLASS);
                        apiMethod = apiClass.getMethod("api");
                    } catch (Throwable ignored) {
                        apiMethod = null;
                    } finally {
                        probed = true;
                    }
                }
            }
        }
        Method method = apiMethod;
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object unwrapOptional(Object maybeOptional) {
        if (maybeOptional instanceof Optional<?> opt) {
            return opt.orElse(null);
        }
        return maybeOptional;
    }

    private static Object invoke(Object target, String name) throws Exception {
        Method method = target.getClass().getMethod(name);
        return method.invoke(target);
    }

    private static Object invoke(Object target, String name, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = target.getClass().getMethod(name, paramTypes);
        return method.invoke(target, args);
    }

    private static boolean invokeBoolean(Object target, String name) throws Exception {
        Object result = invoke(target, name);
        return result instanceof Boolean flag && flag;
    }

    private static boolean invokeOptionalBoolean(Object target, String name) {
        try {
            return invokeBoolean(target, name);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
