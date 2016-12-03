package com.ezrol.terry.minecraft.wasteland_meteors;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ezterry on 12/1/16.
 */
public class WeakCache {
    private List<WeakReference<Object>> cache;
    private int mxsz=1000;

    public WeakCache(int maxsize){
        cache=new LinkedList<>();
        mxsz=maxsize;
    }
    public void addObject(Object o){
        cache.add(0,new WeakReference<>(o));
    }
    public List<Object> currentList(){
        Iterator<WeakReference<Object>> i;
        List<Object> r=new LinkedList<>();
        WeakReference<Object> ref;
        Object e;
        int cnt=0;

        for(i=cache.iterator();i.hasNext();){
            ref=i.next();
            e=ref.get();
            if(e == null){
                i.remove();
            }
            else{
                if(cnt==mxsz){
                    i.remove();
                }
                else {
                    r.add(e);
                    cnt += 1;
                }
            }
        }
        return(r);
    }
}
