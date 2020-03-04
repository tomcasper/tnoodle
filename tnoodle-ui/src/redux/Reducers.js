import { ActionTypes } from "./Types";
import { defaultWcif } from "../constants/default.wcif";
import { MBLD_DEFAULT } from "../constants/wca.constants";
import {
    competitionName2Id,
    competitionName2ShortName
} from "../util/competition.name.util";

const defaultStore = {
    wcif: defaultWcif,
    mbld: MBLD_DEFAULT,
    password: "",
    editingDisabled: false,
    officialZip: true,
    fileZipBlob: null
};

export const Reducer = (store, action) => {
    if (action.type === ActionTypes.UPDATE_ME) {
        return {
            ...store,
            me: action.payload.me
        };
    }

    if (action.type === ActionTypes.UPDATE_EVENTS) {
        return {
            ...store,
            wcif: { ...store.wcif, events: action.payload.events }
        };
    }

    if (action.type === ActionTypes.UPDATE_PASSWORD) {
        return {
            ...store,
            password: action.payload.password
        };
    }

    if (action.type === ActionTypes.UPDATE_COMPETITION_NAME) {
        let competitionName = action.payload.competitionName;
        let shortName = competitionName2ShortName(competitionName);
        let id = competitionName2Id(competitionName);
        return {
            ...store,
            wcif: {
                ...store.wcif,
                name: competitionName,
                shortName: shortName,
                id: id
            }
        };
    }

    if (action.type === ActionTypes.UPDATE_WCA_EVENT) {
        return {
            ...store,
            wcif: {
                ...store.wcif,
                events: [
                    ...store.wcif.events.filter(
                        wcaEvent => wcaEvent.id !== action.payload.wcaEvent.id
                    ),
                    action.payload.wcaEvent
                ]
            }
        };
    }

    if (action.type === ActionTypes.UPDATE_MBLD) {
        return {
            ...store,
            mbld: action.payload.mbld
        };
    }

    if (action.type === ActionTypes.UPDATE_COMPETITIONS) {
        return {
            ...store,
            competitions: action.payload.competitions
        };
    }

    /**
     * Either sets or reset WCIF to default.
     */
    if (action.type === ActionTypes.UPDATE_WCIF) {
        return {
            ...store,
            wcif: action.payload.wcif || defaultWcif
        };
    }

    if (action.type === ActionTypes.UPDATE_EDITING_STATUS) {
        return {
            ...store,
            editingDisabled: action.payload.editingDisabled
        };
    }

    if (action.type === ActionTypes.UPDATE_COMPETITION_ID) {
        return { ...store, competitionId: action.payload.competitionId };
    }

    if (action.type === ActionTypes.UPDATE_OFFICIAL_ZIP_STATUS) {
        return { ...store, officialZip: action.payload.officialZip };
    }

    if (action.type === ActionTypes.UPDATE_FILE_ZIP_BLOB) {
        return { ...store, fileZipBlob: action.payload.fileZipBlob };
    }

    return store || defaultStore;
};