package hk.hku.cs.toiletinator1000

import androidx.fragment.app.Fragment

class ReviewUtil {
    fun Fragment.addChildFragment(fragment: Fragment, frameId: Int) {

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(frameId, fragment).commit()
    }
}