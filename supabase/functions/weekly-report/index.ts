import { createClient } from "https://esm.sh/@supabase/supabase-js@2.7.1";

Deno.serve(async (_req) => {
  try {
    console.log("Generating weekly reports...");
    
    const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? '';
    const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '';
    
    const supabase = createClient(supabaseUrl, supabaseKey);
    
    const { data: profiles, error: fetchError } = await supabase
      .from('profiles')
      .select('id, full_name');
      
    if (fetchError) throw fetchError;
    
    const reports: any[] = [];
    
    for (const profile of (profiles || [])) {
      const oneWeekAgo = new Date();
      oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);
      
      const { data: prayers } = await supabase
        .from('prayer_logs')
        .select('*')
        .eq('user_id', profile.id)
        .eq('status', 'prayed')
        .gte('created_at', oneWeekAgo.toISOString());
        
      const prayedCount = prayers?.length || 0;
      
      const { data: medicineLogs } = await supabase
        .from('medicine_logs')
        .select('*')
        .eq('user_id', profile.id)
        .gte('scheduled_at', oneWeekAgo.toISOString());
        
      const totalMeds = medicineLogs?.length || 0;
      const takenMeds = medicineLogs?.filter(l => l.status === 'taken')?.length || 0;
      const adherenceRate = totalMeds > 0 ? Math.round((takenMeds / totalMeds) * 100) : 100;
      
      const reportMessage = `আসসালামু আলাইকুম, ${profile.full_name || 'ব্যবহারকারী'}। গত সপ্তাহে আপনি ${prayedCount} ওয়াক্ত নামাজ পড়েছেন এবং ওষুধের নিয়ম মেনে চলার হার ছিল ${adherenceRate}%। আল্লাহ আপনার আমল কবুল করুন।`;
      
      const { data: notif } = await supabase.from('notifications').insert([
        {
          user_id: profile.id,
          title: 'সাপ্তাহিক প্রতিবেদন',
          message: reportMessage,
          type: 'hadith',
          is_read: false,
          created_at: new Date().toISOString()
        }
      ]).select();
      
      if (notif) reports.push(...notif);
    }

    return new Response(
      JSON.stringify({ 
        success: true, 
        message: `Weekly reports processed successfully. Generated: ${reports.length}`,
        reports: reports
      }),
      { headers: { "Content-Type": "application/json" } }
    );
  } catch (error) {
    console.error("Error in weekly-report:", error);
    return new Response(
      JSON.stringify({ error: (error as any).message }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }
});
