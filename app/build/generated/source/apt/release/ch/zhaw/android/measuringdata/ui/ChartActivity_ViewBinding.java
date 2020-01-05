// Generated code from Butter Knife. Do not modify!
package ch.zhaw.android.measuringdata.ui;

import android.view.View;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import ch.zhaw.android.measuringdata.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ChartActivity_ViewBinding implements Unbinder {
  private ChartActivity target;

  @UiThread
  public ChartActivity_ViewBinding(ChartActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public ChartActivity_ViewBinding(ChartActivity target, View source) {
    this.target = target;

    target.lineChartView = Utils.findRequiredView(source, R.id.line_chart, "field 'lineChartView'");
  }

  @Override
  @CallSuper
  public void unbind() {
    ChartActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.lineChartView = null;
  }
}
