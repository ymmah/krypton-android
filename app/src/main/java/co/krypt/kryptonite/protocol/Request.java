package co.krypt.kryptonite.protocol;

import android.support.constraint.ConstraintLayout;
import android.widget.RemoteViews;

import com.amazonaws.util.Base32;
import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

import javax.annotation.Nullable;

import co.krypt.kryptonite.R;
import co.krypt.kryptonite.crypto.SHA256;
import co.krypt.kryptonite.exception.CryptoException;
import co.krypt.kryptonite.pairing.Pairing;

/**
 * Created by Kevin King on 12/3/16.
 * Copyright 2016. KryptCo, Inc.
 */

public class Request {
    @SerializedName("request_id")
    @JSON.JsonRequired
    public String requestID;

    public String requestIDCacheKey(Pairing pairing) throws CryptoException {
        return Base32.encodeAsString(SHA256.digest((pairing.getUUIDString().toLowerCase() + requestID).getBytes())).toLowerCase().replace("=", "-");
    }

    @SerializedName("v")
    @JSON.JsonRequired
    public String version;

    @SerializedName("unix_seconds")
    @JSON.JsonRequired
    public Long unixSeconds;


    @SerializedName("a")
    @Nullable
    public Boolean sendACK;

    public RequestBody body;

    public Version semVer() {
        try {
            return Version.valueOf(version);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return Version.valueOf("0.0.0");
    }

    public void fillRemoteViews(RemoteViews remoteViewsContainer, @Nullable Boolean approved, @Nullable String signature) {
        remoteViewsContainer.removeAllViews(R.id.content);
        body.visit(new RequestBody.Visitor<Void, RuntimeException>() {
            @Override
            public Void visit(MeRequest meRequest) throws RuntimeException {
                return null;
            }

            @Override
            public Void visit(SignRequest signRequest) throws RuntimeException {
                signRequest.fillRemoteViews(remoteViewsContainer, approved, signature);
                return null;
            }

            @Override
            public Void visit(GitSignRequest gitSignRequest) throws RuntimeException {
                gitSignRequest.fillRemoteViews(remoteViewsContainer, approved, signature);
                return null;
            }

            @Override
            public Void visit(UnpairRequest unpairRequest) throws RuntimeException {
                return null;
            }

            @Override
            public Void visit(HostsRequest hostsRequest) throws RuntimeException {
                return null;
            }
        });
    }

    public void fillShortRemoteViews(RemoteViews remoteViewsContainer, @Nullable Boolean approved, @Nullable String signature) {
        remoteViewsContainer.removeAllViews(R.id.content);
        body.visit(new RequestBody.Visitor<Void, RuntimeException>() {
            @Override
            public Void visit(MeRequest meRequest) throws RuntimeException {
                return null;
            }

            @Override
            public Void visit(SignRequest signRequest) throws RuntimeException {
                signRequest.fillShortRemoteViews(remoteViewsContainer, approved, signature);
                return null;
            }

            @Override
            public Void visit(GitSignRequest gitSignRequest) throws RuntimeException {
                gitSignRequest.fillShortRemoteViews(remoteViewsContainer, approved, signature);
                return null;
            }

            @Override
            public Void visit(UnpairRequest unpairRequest) throws RuntimeException {
                return null;
            }

            @Override
            public Void visit(HostsRequest hostsRequest) throws RuntimeException {
                return null;
            }
        });
    }

    public void fillView(ConstraintLayout content) {
        body.visit(new RequestBody.Visitor<Void, RuntimeException>() {
            @Override
            public Void visit(MeRequest meRequest) throws RuntimeException {
                return null;
            }

            @Override
            public Void visit(SignRequest signRequest) throws RuntimeException {
                signRequest.fillView(content);
                return null;
            }

            @Override
            public Void visit(GitSignRequest gitSignRequest) throws RuntimeException {
                gitSignRequest.fillView(content);
                return null;
            }

            @Override
            public Void visit(UnpairRequest unpairRequest) throws RuntimeException {
                return null;
            }

            @Override
            public Void visit(HostsRequest hostsRequest) throws RuntimeException {
                return null;
            }
        });
    }

    public String analyticsCategory() {
        return body.visit(new RequestBody.Visitor<String, RuntimeException>() {
            @Override
            public String visit(MeRequest meRequest) throws RuntimeException {
                return "me";
            }

            @Override
            public String visit(SignRequest signRequest) throws RuntimeException {
                return "signature";
            }

            @Override
            public String visit(GitSignRequest gitSignRequest) throws RuntimeException {
                return gitSignRequest.analyticsCategory();
            }

            @Override
            public String visit(UnpairRequest unpairRequest) throws RuntimeException {
                return "unpair";
            }

            @Override
            public String visit(HostsRequest hostsRequest) throws RuntimeException {
                return "hosts";
            }
        });
    }

    public static class Serializer implements JsonSerializer<Request> {
        @Override
        public JsonElement serialize(Request src, Type typeOfSrc, JsonSerializationContext context) {
            JsonElement j = JSON.gson.toJsonTree(src);
            JsonObject o = j.getAsJsonObject();
            src.body.visit(new RequestBody.Visitor<Void, RuntimeException>() {
                @Override
                public Void visit(MeRequest meRequest) {
                    o.add(MeRequest.FIELD_NAME, context.serialize(meRequest));
                    return null;
                }

                @Override
                public Void visit(SignRequest signRequest) {
                    o.add(SignRequest.FIELD_NAME, context.serialize(signRequest));
                    return null;
                }

                @Override
                public Void visit(GitSignRequest gitSignRequest) {
                    o.add(GitSignRequest.FIELD_NAME, context.serialize(gitSignRequest));
                    return null;
                }

                @Override
                public Void visit(UnpairRequest unpairRequest) {
                    o.add(UnpairRequest.FIELD_NAME, context.serialize(unpairRequest));
                    return null;
                }

                @Override
                public Void visit(HostsRequest hostsRequest) {
                    o.add(HostsRequest.FIELD_NAME, context.serialize(hostsRequest));
                    return null;
                }
            });
            return o;
        }
    }

    public static class Deserializer implements JsonDeserializer<Request> {
        @Override
        public Request deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Request request = JSON.gsonWithoutRequiredFields.fromJson(json, typeOfT);
            //TODO: does not use correct deserializer for gitsignrequestbody
            request.body = new RequestBody.Deserializer().deserialize(json, typeOfT, context);
            JSON.checkPojoRecursively(request);
            return request;
        }
    }
}
