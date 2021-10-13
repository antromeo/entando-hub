import {Loading, Modal} from "carbon-components-react"
import UpdateBundleGroup from "./update-boundle-group/UpdateBundleGroup"
import {useCallback, useEffect, useState} from "react"
import {
    addNewBundle,
    editBundleGroup, getAllBundlesForABundleGroup,
    getAllCategories,
    getSingleBundleGroup, getSingleOrganisation
} from "../../../../integration/Integration"
import {getProfiledUpdateSelectStatusInfo} from "../../../../helpers/profiling";
import {getHigherRole} from "../../../../helpers/helpers";


export const ModalUpdateBundleGroup = ({bundleGroupId, open, onCloseModal, onAfterSubmit}) => {


    const [organisation, setOrganisation] = useState({organisationId: "", name: ""})
    const [children, setChildren] = useState([])
    const [categories, setCategories] = useState([])

    const [bundleGroup, setBundleGroup] = useState({})
    const [passiveModal, setPassiveModal] = useState(false)
    const [loading, setLoading] = useState(false)

    const [selectValuesInfo, setSelectValuesInfo] = useState([])

    const onDataChange = useCallback((bundleGroup) => {
        setBundleGroup(bundleGroup)
    }, [])

    const onRequestClose = (e) => {
        onCloseModal()
    }

    useEffect(() => {
        let isMounted = true
        setLoading(false)

        const initCG = async () => {
            const res = await getAllCategories()
            if (isMounted) {
                setCategories(res.categoryList)
            }
        }
        const initBG = async () => {
            const res = await getSingleBundleGroup(bundleGroupId)
            const childrenFromDb = res.bundleGroup.children && res.bundleGroup.children.length > 0
                ? (await getAllBundlesForABundleGroup(bundleGroupId)).bundleList
                : []

            const organisation = (await getSingleOrganisation(res.bundleGroup.organisationId)).organisation
            if (isMounted) {
                if (organisation) setOrganisation(organisation)
                let bg = {
                    ...res.bundleGroup,
                    children: childrenFromDb,
                }
                let selectStatusInfo = getProfiledUpdateSelectStatusInfo(getHigherRole(), bg.status);
                setSelectValuesInfo(selectStatusInfo)
                setPassiveModal(selectStatusInfo.disabled)
                setBundleGroup(bg)
                setChildren(childrenFromDb)
            }
        }

        (async ()=>{
            await Promise.all([initCG(),initBG()])
            setLoading(true)
        })()
        return () => {
            isMounted = false
        }

    }, [bundleGroupId])


    //TODO BE QUERY REFACTORING
    const updateBundleGroup = async (bundleGroup) => {
        let newChildren = []
        if (bundleGroup.children && bundleGroup.children.length) {
            //call addNewBundle rest api, saving every bundle
            //WARNING a new bundle is created even if already exists
            //the call is async in respArray there will be the new bundles id
            let respArray = await Promise.all(bundleGroup.children.map(addNewBundle))
            newChildren = respArray.map(res => res.newBundle.data.bundleId)
        }
        const toSend = {
            ...bundleGroup,
            children: newChildren
        }
        await editBundleGroup(toSend, toSend.bundleGroupId)
    }

    const onRequestSubmit = (e) => {
        (async () => {
            await updateBundleGroup(bundleGroup)
            onCloseModal()
            onAfterSubmit()

        })()
    }

    return (
        <>
            {!loading && <Loading/>}
            {loading && <Modal
                passiveModal={passiveModal}
                modalLabel="Edit"
                primaryButtonText="Save"
                secondaryButtonText="Cancel"
                open={open}
                onRequestClose={onRequestClose}
                onRequestSubmit={onRequestSubmit}>
                <UpdateBundleGroup organisation={organisation} categories={categories} children={children} onDataChange={onDataChange} bundleGroup={bundleGroup} selectValuesInfo={selectValuesInfo}/>
            </Modal>}
        </>
    )
}
