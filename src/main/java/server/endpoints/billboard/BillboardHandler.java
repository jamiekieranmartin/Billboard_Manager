package server.endpoints.billboard;


import common.Status;
import common.models.Billboard;
import common.models.Request;
import common.models.Response;
import server.database.DataService;
import server.middleware.MiddlewareHandler;

import java.sql.SQLException;

/**
 * This class handles how the billboard endpoints interact with the billboard database handler
 * and the client.
 *
 * @author Perdana Bailey
 */
public class BillboardHandler {

    DataService db;
    MiddlewareHandler middlewareHandler;

    /**
     * The BillboardHandler Constructor.
     *
     * @param db: This is the DataService handler that connects the Endpoint to the database.
     */
    public BillboardHandler(DataService db, MiddlewareHandler middlewareHandler) {
        this.db = db;
        this.middlewareHandler = middlewareHandler;
    }

    /**
     * The Endpoint BillboardHandler get method, used to list billboards or retrieve a singular
     * billboard.
     *
     * @param data: This is used as the parameter for the method being used.
     * @param <T>:  This generic determines whether to retrieve a list of billboards or a single
     *              billboard.
     * @return Response<?>: This is the response to send back to the client.
     */
    private <T> Response<?> get(T data) {
        // Check the object type
        if (data instanceof Boolean) {
            // Check if the request is listing locked or unlocked billboards
            if ((Boolean) data) {
                // Fetch the locked billboards and return the response to the client
                return GetBillboardHandler.getLockedBillboards(this.db);
            } else {
                // Fetch the unlocked billboards and return the response to the client
                return GetBillboardHandler.getUnlockedBillboards(this.db);
            }
        } else if (data instanceof Integer) {
            // get billboard using ID
            return GetBillboardHandler.getBillboardsByID(this.db, (int) data);
        } else if (data instanceof String) {
            // get billboard using billboard name
            return GetBillboardHandler.getBillboardsByName(this.db, (String) data);
        } else if (data == null) {
            // get all
            return GetBillboardHandler.getAllBillboards(this.db);
        } else {
            // return an error response
            return new Response<>(Status.UNSUPPORTED_TYPE, "Unknown parameter received in data field.");
        }
    }

    /**
     * The Endpoint BillboardHandler post method, used to insert billboards into database
     *
     * @param data: This is used as the parameter for the method being used.
     * @param <T>:  This generic determines whether to retrieve a list of billboards or a single
     *              billboard.
     * @return Response<?>: This is the response to send back to the client.
     */
    private <T> Response<?> post(T data) {
        // Check the object data type
        if (data instanceof Billboard) {
            Billboard bb = (Billboard) data;
            // Check if there is an ID
            if (bb.id > 0) {
                // Check the name && userID -> make sure no other names and the user ID exists else return error
                try {
                    if (db.billboards.get(bb.name).isPresent() && db.users.get(bb.userId).isPresent()) {
                        return PostBillboardHandler.insertBillboard(this.db, bb);
                    } else {
                        return new Response<>(Status.BAD_REQUEST, "Billboard object user doesn't exist.");
                    }
                } catch (SQLException e) {
                    // TODO: Console Log this
                    return new Response<>(Status.INTERNAL_SERVER_ERROR, "Failed to add billboard to the database.");
                }
            } else {
                return new Response<>(Status.BAD_REQUEST, "Billboard object contains ID.");
            }
        } else {
            return new Response<>(Status.UNSUPPORTED_TYPE, "Unknown parameter received in data field.");
        }
    }

    /**
     * The Endpoint BillboardHandler delete method, used to delete billboards from the database
     *
     * @param data: This is used as the parameter for the method being used.
     * @param <T>:  This generic determines whether to retrieve a list of billboards or a single
     *              billboard.
     * @return Response<?>: This is the response to send back to the client.
     */
    private <T> Response<?> delete(T data) {
        // Check the object data type
        if (data instanceof Billboard) {
            Billboard bb = (Billboard) data;
            //Check the ID, if ID is larger than 0 -> continue else return error
            if (bb.id > 0) {
                // Check the schedule status, if it's not locked we can delete else return error
                if (!bb.locked) {
                    return DeleteBillboardHandler.deleteBillboard(this.db, bb);
                } else {
                    return new Response<>(Status.BAD_REQUEST, "Can't delete billboard that is scheduled");
                }
            } else {
                return new Response<>(Status.BAD_REQUEST, "Invalid billboard ID.");
            }
        } else {
            return new Response<>(Status.UNSUPPORTED_TYPE, "Unknown parameter received in data field.");
        }
    }

    /**
     * The Endpoint BillboardHandler update method, used to update billboards in the database
     *
     * @param data: This is used as the parameter for the method being used.
     * @param <T>:  This generic determines whether to retrieve a list of billboards or a single
     *              billboard.
     * @return Response<?>: This is the response to send back to the client.
     */
    private <T> Response<?> update(T data) {
        // Check the object data type
        if (data instanceof Billboard) {
            Billboard bb = (Billboard) data;
            //Check the ID, if ID is larger than 0 -> continue else return error
            if (bb.id > 0) {
                // Check the schedule status, if it's not locked we can edit else return error
                if (!bb.locked) {
                    return DeleteBillboardHandler.deleteBillboard(this.db, bb);
                } else {
                    return new Response<>(
                        Status.BAD_REQUEST,
                        "Can't update billboard that is scheduled");
                }

            } else {
                return new Response<>(Status.BAD_REQUEST, "Invalid billboard ID.");
            }

        } else {
            return new Response<>(Status.UNSUPPORTED_TYPE, "Unknown parameter received in data field.");
        }
    }

    /**
     * The Endpoint BillboardHandler route method, used to determine which type of Endpoint
     * BillboardHandler function is needed.
     *
     * @param request: This is the request that needs to be determined.
     * @return Response<?>: This is the response to send back to the client.
     */
    public Response<?> route(Request<?> request) {
        // Check the methods to determine which type of Endpoint BillboardHandler function is needed.
        switch (request.method) {
            case GET_BILLBOARDS:
                // This is the get function, it requires no middleware
                return this.get(request.data);
            case POST_BILLBOARD:
                // Check the token
                if (this.middlewareHandler.checkToken(request.token)) {
                    // Check the permissions
                    if (this.middlewareHandler.checkCanViewBillboard(request.token)) {
                        return this.post(request.data);
                    } else {
                        return new Response<>(Status.UNAUTHORIZED, "User does not have permissions to post billboard.");
                    }
                } else {
                    return new Response<>(Status.UNAUTHORIZED, "Token invalid.");
                }
            default:
                return new Response<>(Status.NOT_FOUND, "Route not found.");
        }
    }

}
