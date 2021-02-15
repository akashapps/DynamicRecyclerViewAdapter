package com.akash.dynamicadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akash.dynamicadapter.enums.SelectionMode
import com.akash.dynamicadapter.enums.SelectionState
import com.akash.dynamicadapter.interfaces.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Functionality list
 * 0. [] Adapter methods
 * 1. [✔] Populate data
 * 2. [✔] Add element / data
 * 3. [✔] Remove element / data
 * 4. [✔] clear
 * 5. [✔] Selection
 * 6. [] Search
 * 7. [✔] Listeners
 * 8. [✔] Get element
 * 9. [✔] Get Position
 * 10.[] Empty state
 */

abstract class BaseAdapter<T : RootObject>() : RecyclerView.Adapter<BaseViewHolder>() {

    var dataList: ArrayList<T> = ArrayList()

    var selectionMode = SelectionMode.None
    private var selectionMap = HashMap<String, T>()
    var selectionListener: AdapterSelectionListener<T>? = null
    var rowClickListeners: ArrayList<OnRowClickAction<T>> = ArrayList()
    var longRowClickListener: ArrayList<OnRowLongClickAction<T>> = ArrayList()

    var currentSearchString: String = ""

    constructor(selectionMode: SelectionMode) : this() {
        this.selectionMode = selectionMode
    }

    constructor(selectionMode: SelectionMode, list: List<T>): this(selectionMode){
        setDataToDisplay(list)
    }

    abstract fun populateDataArrayToDisplay(text: String): ArrayList<T>

    fun populateDataArrayToDisplay(){
        val populateDataArrayToDisplay = populateDataArrayToDisplay("")
        setDataToDisplay(populateDataArrayToDisplay, true)
    }

    fun filterAdapterData(searchText: String){
        if (searchText.isEmpty()){
            return
        }

        if (currentSearchString.equals(searchText)){
            return
        }

        currentSearchString = searchText

        setDataToDisplay(populateDataArrayToDisplay(searchText), true)
    }

    fun addDataToDisplayArray(arrayList: ArrayList<T>){
        addDataToDisplayArray(true, arrayList, false)
    }

    fun addDataToDisplayArray(arrayList: ArrayList<T>, before: Boolean){
        addDataToDisplayArray(true, arrayList, before)
    }

    fun addDataToDisplayArray(allowDuplicate: Boolean, arrayList: ArrayList<T>){
        addDataToDisplayArray(allowDuplicate, arrayList, false)
    }

    fun addDataToDisplayArray(allowDuplicate: Boolean, arrayList: ArrayList<T>, before: Boolean){
        if (arrayList.isEmpty()){
            notifyDataSetChanged()
            return
        }

        if (allowDuplicate){
            insertElement(arrayList, before)
        }else{

            val result = ArrayList<T>()

            for (t in arrayList){
                if (dataList.contains(t)){
                    continue
                }
                result.add(t)
            }

            insertElement(result, before)
        }
    }

    private fun insertElement(arrayList: ArrayList<T>, before: Boolean){
        if (before){
            dataList.addAll(0, arrayList)
        }else{
            dataList.addAll(arrayList)
        }
    }

    fun removeObject(t: T){
        val pos = dataList.indexOf(t)
        if (pos < 0){
            return
        }

        removeObject(t,pos)
    }

    fun removeObject(t : T, position: Int){
        dataList.remove(t)
        notifyItemRemoved(position)
    }

    fun setDataToDisplay(list: List<T>){
        setDataToDisplay(list, false)
    }

    fun setDataToDisplay(list: List<T>, needRefreshAdapter: Boolean){
        dataList.clear()
        dataList.addAll(list)

        if (needRefreshAdapter){
            notifyDataSetChanged()
        }
    }

    fun clearData(){
        dataList.clear()
        notifyDataSetChanged()
    }

    fun getObjectAtPosition(position: Int): T?{
        return dataList.get(position)
    }

    fun getPositionForDisplayedObject(t: T): Int{
        return dataList.indexOf(t)
    }

    //region Abstract Method

    abstract fun getRowLayout(item: T, position: Int): Int
    abstract fun onBindRowViewHolder(item: T, holder: BaseViewHolder, position: Int)
    abstract fun getObjectRowViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder?

    //endregion

    //region Selection

    fun isObjectSelectd(dataObject: T): Boolean{
        if (selectionMode == SelectionMode.None){
            return false
        }

        return selectionMap.get(dataObject.id) != null
    }

    fun selectAllObject(){
        if (itemCount == 0){
            return
        }

        if (selectionMode != SelectionMode.Multiple){
            return
        }

        val selectedList = ArrayList<T>()
        for (i in 0 until dataList.size){
            val objectAtPosition = getObjectAtPosition(i)

            objectAtPosition?.let {
                val isSelected = isObjectSelectd(it)
                if (isSelected == false){
                    selectedList.add(it)
                    selectionMap.put(it.id, it)
                }
            }
        }

        selectionListener?.let {
            it.adapterSelectionChanged(selectedList, true)
        }

        notifyDataSetChanged()
    }

    fun unSelectAllObject(){
        if (itemCount == 0){
            return
        }

        val selectedList = ArrayList<T>()
        for (i in 0 until dataList.size){
            val objectAtPosition = getObjectAtPosition(i)

            objectAtPosition?.let {
                val isSelected = isObjectSelectd(it)
                if (isSelected){
                    selectedList.add(it)
                    selectionMap.remove(it.id)
                }
            }
        }

        selectionListener?.let {
            it.adapterSelectionChanged(selectedList, true)
        }

        notifyDataSetChanged()
    }

    fun changeSelectionOfObject(item: T, isSelected: Boolean){
        val list = ArrayList<T>().apply {
            add(item)
        }
        changeSelectionOfObjects(list, isSelected)
    }

    fun changeSelectionOfObjects(list: ArrayList<T>, selection: Boolean){
        val changedArray = ArrayList<T>()

        for (t in list){
            val objectSelected = isObjectSelectd(t)

            if ((selection && objectSelected) || (!selection && !objectSelected)){
                return
            }

            val position = getPositionForDisplayedObject(t)

            if (position < 0){
                return
            }

            if (selection){
                selectionMap.put(t.id, t)
            }else{
                selectionMap.remove(t.id)
            }

            notifyItemChanged(position)
            changedArray.add(t)
        }

        if (changedArray.size <= 0){
            return
        }

        selectionListener?.adapterSelectionChanged(changedArray, selection)
    }

    fun toggleSelectionOfObject(t: T){
        if (selectionMode == SelectionMode.None){
            return
        }

        if (selectionMode == SelectionMode.Single){

            var previous : T? = null

            if (selectionMap.size > 0){
                previous = selectionMap.values.first()
            }

            selectionMap.clear()

            previous?.let {
                broadcastSelectionListener(it, false)
                if (previous.id == t.id){
                    return
                }
            }

            selectionMap.put(t.id, t)
            broadcastSelectionListener(t, true)
            return
        }
        //TODO:: Need to handle multiple selection category
    }

    fun isAllObjectSelected(): Boolean{
        if (selectionMode == SelectionMode.None){
            return false
        }

        return itemCount == selectionMap.size
    }

    fun getSelectedObjects(): ArrayList<T>{
        return ArrayList(selectionMap.values)
    }

    fun getNumberOfSelectedObjects(): Int{
        if (selectionMode == SelectionMode.None){
            return 0
        }

        return selectionMap.size
    }

    //endregion

    //region Adapter Method

    override fun getItemViewType(position: Int): Int {

        val data = getObjectAtPosition(position)

        data?.let {
            return getRowLayout(it, position)
        }

        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        var baseViewHolder = getObjectRowViewHolder(parent, viewType)

        baseViewHolder?.let {
            val view = LayoutInflater.from(parent.context).inflate(viewType, null)
            baseViewHolder = BaseViewHolder(view)
        }

        baseViewHolder!!.onClickListener = object : OnClickListener {
            override fun onClick(viewHolder: BaseViewHolder, position: Int) {

                val data = getObjectAtPosition(position)

                data?.let {
                    if (selectionMode == SelectionMode.None){
                        broadcastRowClickListener(it, position)
                    }else{
                        toggleSelectionOfObject(it)
                        notifyItemChanged(position)
                    }
                }
            }
        }

        baseViewHolder!!.onLongClickListener = object : OnLongClickListener {
            override fun onLongClick(viewHolder: BaseViewHolder, position: Int) {
                val data = getObjectAtPosition(position)

                data?.let {
                    broadcastRowLongClickListener(it, position)
                }
            }
        }

        return baseViewHolder!!
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getObjectAtPosition(position) ?: return

        onBindRowViewHolder(item, holder, position)

        var selectionState = SelectionState.Unselected

        if (selectionMode == SelectionMode.None){
            if (isObjectSelectd(item)){
                selectionState = SelectionState.Selected
            }
           holder.selectionState = selectionState
        }else{
            holder.selectionState = SelectionState.Hidden
        }
    }

    // endregion

    fun isSearchInProgress(): Boolean{
        return currentSearchString.isNullOrEmpty();
    }

    // region BroadCase Listeners

    private fun broadcastSelectionListener(obj: T, isSelected: Boolean){
        broadcastSelectionListener(ArrayList<T>().apply { add(obj) }, isSelected)
    }

    private fun broadcastSelectionListener(list: ArrayList<T>, isSelected: Boolean){
        selectionListener?.let {
            it.adapterSelectionChanged(list, isSelected)
        }
    }

    private fun broadcastRowClickListener(data: T, position: Int){
        for (item in rowClickListeners){
            item.rowClickAction(data, position)
        }
    }

    private fun broadcastRowLongClickListener(data: T, position: Int){
        for (item in longRowClickListener){
            item.rowLongClickAction(data, position)
        }
    }

    //endregion


}